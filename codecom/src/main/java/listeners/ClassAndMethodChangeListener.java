package listeners;

import action.CommentAction;
import cache.CommentStatusCache;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.impl.source.PsiMethodImpl;
import method.TreeBuilder;
import model.AbstractTreeNode;
import model.ClassNode;
import model.MethodNode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.PsiElementTreeUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import java.util.ArrayList;
import java.util.List;

import static utils.PsiElementTreeUtil.repaintTreeAndKeepExpanded;

public class ClassAndMethodChangeListener extends PsiTreeChangeAdapter {
    private final Project project;
    private final TreeBuilder treeBuilder;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassAndMethodChangeListener.class);


    public ClassAndMethodChangeListener(Project project, TreeBuilder treeBuilder) {
        this.project = project;
        this.treeBuilder = treeBuilder;
    }

    @Override
    public void childAdded(PsiTreeChangeEvent event) {
        PsiElement eventChild = event.getChild();
        PsiElement eventParent = event.getParent();
        LOGGER.info("child added - element: {} | child: {} | parent: {}", event.getElement(), eventChild.getClass(), eventParent.getClass());

        if (eventChild instanceof PsiMethodImpl) {
            addPsiMethodToTree(eventChild, eventParent);
        } else if (eventChild instanceof PsiClassImpl) {
            addPsiClassToTree(eventChild, eventParent);
        }
    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {
        PsiElement eventChild = event.getChild();
        LOGGER.info("child removed - {}, {}", eventChild.getClass(), eventChild.getChildren());

        if (eventChild instanceof PsiMethodImpl) {
            removePsiElementFromTree(eventChild);
        } else if (eventChild instanceof PsiClassImpl) {
            removePsiElementFromTree(eventChild);
        }
        else if(eventChild instanceof PsiJavaFileImpl psiJavaFile){
            for(PsiClass psiClass : psiJavaFile.getClasses()){
                removePsiElementFromTree(psiClass);
            }
        }
    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {
        PsiElement element = event.getChild();
        LOGGER.info("child replaced - element: {},oldChild: {}, child : {}, parent: {}" ,event.getElement(), event.getOldChild(), element.getClass(), event.getParent());
        repaintTreeAndKeepExpanded(treeBuilder.getMethodTree());
    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {
        PsiElement element = event.getChild();
        LOGGER.info("child moved - {}" , element.getClass());
    }

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        PsiElement element = event.getParent();
        LOGGER.info("children changed - {}, {}" , element.getClass(), element.getChildren());
    }

    private void addPsiMethodToTree(PsiElement psiMethod, PsiElement eventParent) {
        JTree tree = treeBuilder.getMethodTree();
        TreePath parentPath = PsiElementTreeUtil.findPathForPsiElement(tree, eventParent);

        if (parentPath != null) {
            AbstractTreeNode<?> parentNode = (AbstractTreeNode<?>) parentPath.getLastPathComponent();
            PsiMethod method = (PsiMethod) psiMethod;
            MethodNode newMethodNode = new MethodNode(method);
            parentNode.add(newMethodNode);
            treeBuilder.reload();
        } else {
            LOGGER.warn("Parent node not found for PSI element: {}", eventParent);
        }
    }

    private void addPsiClassToTree(PsiElement psiClassElement, PsiElement eventParent) {
        JTree tree = treeBuilder.getMethodTree();
        if(eventParent instanceof PsiJavaFile psiJavaFile) {
            eventParent = psiJavaFile.getParent();
        }
        TreePath parentPath = PsiElementTreeUtil.findPathForPsiElement(tree, eventParent);
        if (parentPath == null) {
            LOGGER.info("parentPathIsNULL");
        } else {
            LOGGER.info(parentPath.toString());
        }
        if (parentPath != null) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
            PsiClass psiClass = (PsiClass) psiClassElement;
            ClassNode newClassNode = new ClassNode(psiClass);
            parentNode.add(newClassNode);
            treeBuilder.reload();
        } else {
            LOGGER.warn("Parent node not found for PSI element: {}", eventParent);
        }
    }

    private void addPsiJavaFileToTree(PsiElement psiJavaFileElement, PsiElement eventParent){

    }

    private void removePsiElementFromTree(PsiElement psiElement) {
        JTree tree = treeBuilder.getMethodTree();
        TreePath elementPath = PsiElementTreeUtil.findPathForPsiElement(tree, psiElement);

        if (elementPath != null) {
            DefaultMutableTreeNode elementNode = (DefaultMutableTreeNode) elementPath.getLastPathComponent();
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) elementNode.getParent();
            if (parentNode != null) {
                parentNode.remove(elementNode);
                treeBuilder.reload(parentNode);

                removeAllDescendants(elementNode);
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
