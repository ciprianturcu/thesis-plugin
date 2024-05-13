package method;

import cache.CommentStatusCache;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.ui.treeStructure.Tree;
import model.ClassNode;
import model.DirectoryNode;
import model.MethodNode;
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

//    public void addChildToParent(PsiElement childElement, PsiElement parentElement) {
//        SwingUtilities.invokeLater(() -> {
//            DefaultMutableTreeNode parentNode = getNodeByPsiElement(parentElement);
//            if (parentNode == null) {
//                // Parent node not found
//                return;
//            }
//
//            // Create a new node for the child element
//            TreeNodeData childNodeData = new TreeNodeData(childElement.toString(), TreeNodeData.NodeType.METHOD, childElement); // Modify as per actual type logic
//            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childNodeData);
//
//            // Add the child to the parent node
//            parentNode.add(childNode);
//
//            // Notify the model that nodes have been inserted
//            ((DefaultTreeModel) methodTree.getModel()).nodesWereInserted(parentNode, new int[]{parentNode.getChildCount() - 1});
//
//            // Optional: Ensure the tree is expanded to show the newly added node
//            methodTree.expandPath(new TreePath(parentNode.getPath()));
//        });
//    }


    private DefaultMutableTreeNode getNodeByPsiElement(DefaultMutableTreeNode currentNode, PsiElement element) {
        // Check if the current node's user object is the element we're looking for
        if (((TreeNodeData) currentNode.getUserObject()).getPsiElement().equals(element)) {
            return currentNode;
        }

        // Recursively search children
        for (int i = 0; i < currentNode.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) currentNode.getChildAt(i);
            DefaultMutableTreeNode resultNode = getNodeByPsiElement(childNode, element);
            if (resultNode != null) {
                return resultNode;
            }
        }

        return null;  // Not found
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
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DirectoryNode(psiDirectory.getName(), psiDirectory));
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
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(new ClassNode(psiClass.getName(), psiClass));
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                classNode.add(new DefaultMutableTreeNode(new MethodNode(psiMethod.getName(), psiMethod)));
                CommentStatusCache.getInstance().addMethod(psiMethod);
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
