package method;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.ui.treeStructure.Tree;
import listeners.ClassAndMethodChangeListener;
import model.AbstractTreeNode;
import model.ClassNode;
import model.DirectoryNode;
import model.MethodNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import java.util.Arrays;
import java.util.List;

import static utils.PsiElementTreeUtil.restoreExpansionState;
import static utils.PsiElementTreeUtil.saveExpansionState;

public class TreeBuilder implements TreeSelectionListener {
    private final Project project;
    private final DefaultMutableTreeNode root;
    private final JTree methodTree;

    private static final Logger LOGGER =Logger.getInstance(ClassAndMethodChangeListener.class);


    public TreeBuilder(Project project) {
        this.project = project;
        this.root = new DefaultMutableTreeNode("Project modules");
        this.methodTree = new Tree(new SortedTreeModel(root));
        this.methodTree.setCellRenderer(new CustomColoredTreeCellRenderer());
        this.methodTree.addTreeSelectionListener(this);
        methodTree.setSelectionModel(getCustomSelectionModel());
        methodTree.setShowsRootHandles(true);
        methodTree.setRootVisible(false);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        AbstractTreeNode<?> selectedNode = (AbstractTreeNode<?>) methodTree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            if (selectedNode.getPsiElement() instanceof Navigatable) {
                ((Navigatable) selectedNode.getPsiElement()).navigate(true);
            }
        }
    }

    @NotNull
    private DefaultTreeSelectionModel getCustomSelectionModel() {
        return new DefaultTreeSelectionModel() {
            @Override
            public void setSelectionPath(TreePath path) {
                if (isRootNode(path)) {
                    super.setSelectionPath(path);
                }
            }

            @Override
            public void addSelectionPath(TreePath path) {
                if (isRootNode(path)) {
                    super.addSelectionPath(path);
                }
            }

            @Override
            public void setSelectionPaths(TreePath[] paths) {
                TreePath[] filteredPaths = filterRootNode(paths);
                super.setSelectionPaths(filteredPaths);
            }

            @Override
            public void addSelectionPaths(TreePath[] paths) {
                TreePath[] filteredPaths = filterRootNode(paths);
                super.addSelectionPaths(filteredPaths);
            }

            private boolean isRootNode(TreePath path) {
                return path == null || path.getLastPathComponent() != root;
            }

            private TreePath[] filterRootNode(TreePath[] paths) {
                return Arrays.stream(paths)
                        .filter(this::isRootNode)
                        .toArray(TreePath[]::new);
            }
        };
    }


    public JTree getMethodTree() {
        return methodTree;
    }

    public void buildMethodTree() {
        List<TreePath> expansionState = saveExpansionState(methodTree);

        removeAllDescendants(root);

        ApplicationManager.getApplication().executeOnPooledThread(()->{
            startDepthFirstSearch(project);

            SwingUtilities.invokeLater(() -> {
                ((DefaultTreeModel) methodTree.getModel()).reload(); // Refresh the tree model on the EDT
                restoreExpansionState(methodTree, expansionState);
            });
        });
    }

    public void startDepthFirstSearch(Project project) {
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiManager psiManager = PsiManager.getInstance(project);

            ProjectRootManager.getInstance(project).getFileIndex().iterateContent(fileOrDir -> {
                if (fileOrDir.isDirectory()) {
                    PsiDirectory psiDirectory = psiManager.findDirectory(fileOrDir);
                    if (psiDirectory != null) {
                        root.add(depthFirstSearch(psiDirectory));  // Start DFS from each root directory
                    }
                    return false;  // Return false to stop processing this branch with iterateContent
                }
                return true;
            });
        });
    }

    public DefaultMutableTreeNode depthFirstSearch(PsiDirectory psiDirectory) {
        DirectoryNode node = new DirectoryNode(psiDirectory);
        boolean hasJavaFile = false;

        for(PsiFile psiFile : psiDirectory.getFiles())
        {
            if (psiFile.getName().endsWith(".java")) {

                if (psiFile instanceof PsiJavaFile psiJavaFile) {
                    processPsiJavaFile(node, psiJavaFile);
                }
                hasJavaFile = true;
            }
        }

        // Recursively visit subdirectories
        for (PsiDirectory subdirectory : psiDirectory.getSubdirectories()) {
            DefaultMutableTreeNode childNode = depthFirstSearch(subdirectory);
            if (childNode != null) {
                node.add(childNode);
                hasJavaFile = true;
            }
        }

        return hasJavaFile ? node : null;
    }

    private void processPsiJavaFile(DefaultMutableTreeNode root, final PsiJavaFile psiJavaFile){
        for (PsiClass psiClass : psiJavaFile.getClasses()) {
            DefaultMutableTreeNode classNode = new ClassNode(psiClass);
            addDescendantsOfClass(classNode, psiClass);
            root.add(classNode);
        }
    }

    public void addDescendantsOfClass(DefaultMutableTreeNode parentNode, PsiClass psiClass) {
        for(PsiElement psiElement : psiClass.getChildren()) {
            if(psiElement instanceof PsiClass psiInnerClass) {
                DefaultMutableTreeNode classNode = new ClassNode(psiInnerClass);
                addDescendantsOfClass(classNode, psiInnerClass);
                parentNode.add(classNode);
            } else if (psiElement instanceof PsiMethod psiMethod) {
                DefaultMutableTreeNode methodNode = new MethodNode(psiMethod);
                parentNode.add(methodNode);
            }
        }
    }

    public void removeAllDescendants(DefaultMutableTreeNode root) {
        if (root != null) {
            for (int i = root.getChildCount() - 1; i >= 0; i--) {  // Iterate backwards for efficiency
                removeAllDescendants((DefaultMutableTreeNode) root.getChildAt(i));  // Recursively remove children
            }
            root.removeAllChildren();  // Remove children from current node
        }
    }
}
