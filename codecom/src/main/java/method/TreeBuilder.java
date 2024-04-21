package method;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.ui.treeStructure.Tree;
import model.TreeNodeData;


import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.List;

public class TreeBuilder {
    private final Project project;
    private final DefaultMutableTreeNode root;
    private final JTree methodTree;

    public TreeBuilder(Project project) {
        this.project = project;
        this.root = new DefaultMutableTreeNode("Project modules");
        this.methodTree = new Tree(root);
        this.methodTree.setCellRenderer(new CustomColoredTreeCellRenderer());
        this.methodTree.addTreeSelectionListener(e->{
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) methodTree.getLastSelectedPathComponent();
            if(selectedNode != null && selectedNode.getUserObject() instanceof TreeNodeData treeNodeData){
                if(treeNodeData.getPsiElement() instanceof Navigatable) {
                    ((Navigatable) treeNodeData.getPsiElement()).navigate(true);
                }
            }
        });
    }

    public JTree getMethodTree() {
        return methodTree;
    }

    public void buildMethodTree() {
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
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeNodeData(psiDirectory.getName(), TreeNodeData.NodeType.DIRECTORY, psiDirectory));
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
        List<PsiClass> psiClassesOfFile = getAllClassesOfPsiJavaFile(psiJavaFile);
        for (PsiClass psiClass : psiClassesOfFile) {
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(new TreeNodeData(psiClass.getName(), TreeNodeData.NodeType.CLASS, psiClass));
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                classNode.add(new DefaultMutableTreeNode(new TreeNodeData(psiMethod.getName(), TreeNodeData.NodeType.METHOD, psiMethod)));
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

    public boolean hasComment(PsiMethod method) {
        PsiElement prevSibling = method.getPrevSibling();
        // Navigate backwards over whitespace and comments.
        while ((prevSibling instanceof PsiWhiteSpace || prevSibling instanceof PsiComment)) {
            if (prevSibling instanceof PsiComment) {
                // If any sibling is a comment, the method has a comment.
                return true;
            }
            prevSibling = prevSibling.getPrevSibling();
        }
        // No comment was found before the method.
        return false;
    }

}
