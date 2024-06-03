package method;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.ui.treeStructure.Tree;
import listeners.ClassAndMethodChangeListener;
import model.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TreeBuilder {
    private final Project project;
    private final DefaultMutableTreeNode root;
    private final JTree methodTree;

    private static final Logger LOGGER =Logger.getInstance(ClassAndMethodChangeListener.class);


    public TreeBuilder(Project project) {
        this.project = project;
        this.root = new DefaultMutableTreeNode("Project modules");
        this.methodTree = new Tree(new SortedTreeModel(root));
        this.methodTree.setCellRenderer(new CustomColoredTreeCellRenderer());
        this.methodTree.addTreeSelectionListener(e->{
            AbstractTreeNode<?> selectedNode = (AbstractTreeNode<?>) methodTree.getLastSelectedPathComponent();
            if(selectedNode != null){
                if(selectedNode.getPsiElement() instanceof Navigatable) {
                    ((Navigatable) selectedNode.getPsiElement()).navigate(true);
                }
            }
        });
        // Set custom selection model
        methodTree.setSelectionModel(getCustomSelectionModel());

        // Allow expanding and collapsing nodes
        methodTree.setShowsRootHandles(true);
        methodTree.setRootVisible(false);
    }

    @NotNull
    private DefaultTreeSelectionModel getCustomSelectionModel() {
        return new DefaultTreeSelectionModel() {
            @Override
            public void setSelectionPath(TreePath path) {
                if (!isRootNode(path)) {
                    super.setSelectionPath(path);
                }
            }

            @Override
            public void addSelectionPath(TreePath path) {
                if (!isRootNode(path)) {
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
                return path != null && path.getLastPathComponent() == root;
            }

            private TreePath[] filterRootNode(TreePath[] paths) {
                return Arrays.stream(paths)
                        .filter(path -> !isRootNode(path))
                        .toArray(TreePath[]::new);
            }
        };
    }


    public JTree getMethodTree() {
        return methodTree;
    }

    public void reload(DefaultMutableTreeNode node) {
        DefaultTreeModel test =  (DefaultTreeModel) methodTree.getModel();
        test.reload(node);
    }

    public void reload() {
        DefaultTreeModel test =  (DefaultTreeModel) methodTree.getModel();
        test.reload();
    }

    public void buildMethodTree() {
        root.removeAllChildren();

        startDepthFirstSearch(project);

        SwingUtilities.invokeLater(() -> {
            ((DefaultTreeModel) methodTree.getModel()).reload(); // Refresh the tree model on the EDT
        });
    }

    public void startDepthFirstSearch(Project project) {
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
    }

    public DefaultMutableTreeNode depthFirstSearch(PsiDirectory psiDirectory) {
        DirectoryNode node = new DirectoryNode(psiDirectory);
        boolean hasJavaFile = false;

        for(PsiFile psiFile : psiDirectory.getFiles())
        {
            if (psiFile.getName().endsWith(".java")) {

                if (psiFile instanceof PsiJavaFile psiJavaFile) {
                    //JavaFileNode javaFileNode = new JavaFileNode(psiFile.getName(), psiJavaFile);
                    //node.add(javaFileNode);
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
        List<PsiClass> psiClassesOfFile = getAllClassesOfPsiJavaFile(psiJavaFile);
        for (PsiClass psiClass : psiClassesOfFile) {
            DefaultMutableTreeNode classNode = new ClassNode(psiClass);
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                classNode.add(new MethodNode(psiMethod));
            }
            root.add(classNode);
        }
    }

    // Method to retrieve all classes, including nested ones, from a PsiJavaFile
    public static List<PsiClass> getAllClassesOfPsiJavaFile(PsiJavaFile psiJavaFile) {
        List<PsiClass> allClasses = new ArrayList<>();
        for (PsiClass psiClass : psiJavaFile.getClasses()) {
            addClassesRecursively(psiClass, allClasses);
        }
        return allClasses;
    }

    // Helper method to add classes recursively
    private static void addClassesRecursively(PsiClass psiClass, List<PsiClass> allClasses) {
        allClasses.add(psiClass);
        for (PsiClass innerClass : psiClass.getInnerClasses()) {
            addClassesRecursively(innerClass, allClasses);
        }
    }

}
