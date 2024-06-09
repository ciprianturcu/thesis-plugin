package listeners;

import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.impl.source.PsiUnnamedClassImpl;
import com.intellij.psi.impl.source.javadoc.PsiDocCommentImpl;
import methodTree.TreeBuilder;
import model.AbstractTreeNode;
import model.ClassNode;
import model.DirectoryNode;
import model.MethodNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.PsiElementTreeUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static utils.PsiElementTreeUtil.repaintTreeAndKeepExpanded;

public class ClassAndMethodChangeListener extends PsiTreeChangeAdapter {
    private final TreeBuilder treeBuilder;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassAndMethodChangeListener.class);


    public ClassAndMethodChangeListener(TreeBuilder treeBuilder) {
        this.treeBuilder = treeBuilder;
    }

    @Override
    public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {
        PsiElement eventChild = event.getChild();
        if (eventChild instanceof PsiJavaDirectoryImpl) {
            removePsiElementFromTree(eventChild);
        }
    }

    @Override
    public void childAdded(PsiTreeChangeEvent event) {
        PsiElement eventChild = event.getChild();
        PsiElement eventParent = event.getParent();
        if (eventChild instanceof PsiJavaDirectoryImpl) {
            addPsiDirectory(eventChild, eventParent);
        } else if (eventChild instanceof PsiJavaFileImpl) {
            addPsiJavaFile(eventChild, eventParent);
        } else if (eventChild instanceof PsiClassImpl) {
            addPsiClassToTree(eventChild, eventParent);
        } else if (eventChild instanceof PsiMethodImpl) {
            addPsiMethodToTree(eventChild, eventParent);
        }
    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {
        PsiElement eventChild = event.getChild();
        if (eventChild instanceof PsiDocCommentImpl) {
            refreshMethodCommentStatus(event.getParent());
        } else if (eventChild instanceof PsiMethodImpl) {
            removePsiElementFromTree(eventChild);
        } else if (eventChild instanceof PsiClassImpl) {
            removePsiElementFromTree(eventChild);
        } else if (eventChild instanceof PsiJavaFileImpl psiJavaFile) {
            for (PsiClass psiClass : psiJavaFile.getClasses()) {
                removePsiElementFromTree(psiClass);
            }
        }
    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {
        PsiElement element = event.getChild();
        if (event.getOldChild() instanceof PsiField && element instanceof PsiMethodImpl) {
            addPsiMethodToTree(element, event.getParent());
        }
        if ((event.getOldChild() instanceof PsiDocCommentImpl || event.getChild() instanceof PsiDocCommentImpl) && event.getParent() instanceof PsiMethod) {
            refreshMethodCommentStatus(event.getParent());
        }
        if (event.getOldChild() instanceof PsiModifierList && event.getChild() instanceof PsiClassImpl) {
            addPsiClassToTree(event.getChild(), event.getParent());
        }
        if (event.getOldChild() instanceof PsiClassImpl && !(event.getChild() instanceof PsiUnnamedClassImpl)) {
            removePsiElementFromTree(event.getOldChild());
        }
        if(event.getOldChild() instanceof PsiClassImpl && event.getChild() instanceof  PsiComment) {
            removePsiElementFromTree(event.getOldChild());
        }
        if(event.getOldChild() instanceof PsiComment && event.getChild() instanceof PsiClassImpl)
        {
            addPsiClassToTree(event.getChild(), event.getParent());
        }

    }

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        PsiElement element = event.getParent();
        if (element instanceof PsiMethodImpl) {
            refreshMethodCommentStatus(element);
        }
        if (element instanceof PsiJavaFileImpl) {
            addPsiJavaFile(element, element.getParent());
        }
    }

    private void refreshMethodCommentStatus(PsiElement psiElement) {
        JTree tree = treeBuilder.getMethodTree();
        TreePath elementPath = PsiElementTreeUtil.findPathForPsiElement(tree, psiElement);
        if (elementPath != null) {
            AbstractTreeNode<?> abstractTreeNode = (AbstractTreeNode<?>) elementPath.getLastPathComponent();
            if(abstractTreeNode instanceof MethodNode methodNode)
            {
                methodNode.refreshCommentStatus();
            }
            else{
                LOGGER.warn("Node was not a psi method: {}", psiElement);
            }
        } else {
            LOGGER.warn("Node not found for PSI element: {}", psiElement);
        }
    }

    private void addPsiDirectory(PsiElement psiDirectoryElement, PsiElement eventParent) {
        if (!(psiDirectoryElement instanceof PsiDirectory directory)) return;

        JTree tree = treeBuilder.getMethodTree();
        TreePath parentPath = PsiElementTreeUtil.findPathForPsiElement(tree, eventParent);

        if (parentPath == null) {
            LOGGER.warn("Parent node not found for PSI directory: {}", eventParent);
            return;
        }

        // Initially check if the directory contains any Java files with classes
        boolean containsJavaClasses = false;
        List<PsiJavaFile> javaFiles = new ArrayList<>();
        for (PsiFile file : directory.getFiles()) {
            if (file instanceof PsiJavaFile && ((PsiJavaFile) file).getClasses().length > 0) {
                containsJavaClasses = true;
                javaFiles.add((PsiJavaFile) file);
            }
        }

        // If no Java classes are found, skip adding this directory
        if (!containsJavaClasses) {
            LOGGER.info("Skipping directory as it contains no Java classes: {}", directory.getName());
            return;
        }

        // Check if the directory already exists in the tree
        if (PsiElementTreeUtil.findPathForPsiElement(tree, directory) != null) {
            LOGGER.info("Directory already exists in the tree: {}", directory.getName());
            return; // Skip adding as it's already there
        }

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
        DirectoryNode newDirectoryNode = new DirectoryNode(directory);
        parentNode.add(newDirectoryNode);

        // Add only Java files that contain classes
        for (PsiJavaFile javaFile : javaFiles) {
            for (PsiClass psiClass : javaFile.getClasses()) {
                addPsiClassToTree(psiClass, directory); // Pass newDirectoryNode to maintain correct hierarchy
            }
        }

        repaintTreeAndKeepExpanded(tree);
    }

    private void addPsiJavaFile(PsiElement psiJavaFileElement, PsiElement eventParent) {
        if (!(psiJavaFileElement instanceof PsiJavaFile psiJavaFile)) return;
        JTree tree = treeBuilder.getMethodTree();
        for (PsiClass psiClass : psiJavaFile.getClasses()) {
            if (PsiElementTreeUtil.findPathForPsiElement(tree, psiClass) != null) {
                return; // Skip adding as it's already there

            }
            addPsiClassToTree(psiClass, eventParent);
        }
        repaintTreeAndKeepExpanded(treeBuilder.getMethodTree());
    }

    private void addPsiClassToTree(PsiElement psiClassElement, PsiElement eventParent) {
        if (!(psiClassElement instanceof PsiClass psiClass)) return;

        JTree tree = treeBuilder.getMethodTree();
        Stack<PsiElement> missingParents = new Stack<>();
        PsiElement currentElement = eventParent;

        // Traverse upwards to find the first existing parent in the tree or root, skip file nodes
        while (currentElement != null && PsiElementTreeUtil.findPathForPsiElement(tree, currentElement) == null) {
            if (!(currentElement instanceof PsiJavaFile)) {  // Skip PsiJavaFile
                missingParents.push(currentElement);
            }
            currentElement = getParentElement(currentElement);
        }

        // Get the node corresponding to the first found parent or root
        TreePath parentPath = currentElement == null ? null : PsiElementTreeUtil.findPathForPsiElement(tree, currentElement);
        DefaultMutableTreeNode parentNode = parentPath == null ? null : (DefaultMutableTreeNode) parentPath.getLastPathComponent();

        if (parentNode == null) {
            LOGGER.warn("No valid parent node found in tree for PSI class: {}", psiClass.getName());
            return;
        }

        // Add all missing parents to the tree
        while (!missingParents.isEmpty()) {
            PsiElement elementToAdd = missingParents.pop();
            DefaultMutableTreeNode newNode = createNodeForElement(elementToAdd);
            if (newNode != null) {  // null check for cases where node creation is not applicable
                parentNode.add(newNode);
                parentNode = newNode; // Update the parentNode to the newly added node
            }
        }

        // Finally add the PsiClass itself
        ClassNode newClassNode = new ClassNode(psiClass);
        parentNode.add(newClassNode);

        treeBuilder.addDescendantsOfClass(newClassNode, psiClass);

        repaintTreeAndKeepExpanded(tree);
    }

    private void addPsiMethodToTree(PsiElement psiMethod, PsiElement eventParent) {
        JTree tree = treeBuilder.getMethodTree();
        TreePath parentPath = PsiElementTreeUtil.findPathForPsiElement(tree, eventParent);

        if (parentPath != null) {
            AbstractTreeNode<?> parentNode = (AbstractTreeNode<?>) parentPath.getLastPathComponent();
            PsiMethod method = (PsiMethod) psiMethod;
            MethodNode newMethodNode = new MethodNode(method);
            parentNode.add(newMethodNode);
            repaintTreeAndKeepExpanded(tree);
        } else {
            LOGGER.warn("Parent node not found for PSI element: {}", eventParent);
        }
    }

    private PsiElement getParentElement(PsiElement element) {
        if (element instanceof PsiFile) {
            return ((PsiFile) element).getContainingDirectory();
        } else {
            return element.getParent();
        }
    }

    private @Nullable DefaultMutableTreeNode createNodeForElement(PsiElement element) {
        if (element instanceof PsiDirectory) {
            return new DirectoryNode((PsiDirectory) element);
        } else if (element instanceof PsiClass) {
            return new ClassNode((PsiClass) element);
        }
        return null;
    }

    private void removePsiElementFromTree(PsiElement psiElement) {
        JTree tree = treeBuilder.getMethodTree();
        TreePath elementPath = PsiElementTreeUtil.findPathForPsiElement(tree, psiElement);

        if (elementPath != null) {
            DefaultMutableTreeNode elementNode = (DefaultMutableTreeNode) elementPath.getLastPathComponent();
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) elementNode.getParent();
            if (parentNode != null) {
                parentNode.remove(elementNode);
                removeAllDescendants(elementNode);
                repaintTreeAndKeepExpanded(tree);
            }
        } else {
            LOGGER.warn("Node not found for PSI element: {}", psiElement);
        }
    }

    // Recursive method to remove all descendants of a node
    private void removeAllDescendants(DefaultMutableTreeNode node) {
        // Copy children to avoid ConcurrentModificationException
        List<DefaultMutableTreeNode> children = new ArrayList<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            children.add((DefaultMutableTreeNode) node.getChildAt(i));
        }

        // Recursively remove children
        for (DefaultMutableTreeNode child : children) {
            removeAllDescendants(child);
            child.removeAllChildren(); // This is just to nullify children references
            child.setUserObject(null); // Nullify user object to aid GC
        }

        node.removeAllChildren(); // Finally remove all children of the current node
    }
}
