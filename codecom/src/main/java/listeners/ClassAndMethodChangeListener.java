package listeners;

import action.CommentAction;
import cache.CommentStatusCache;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassImpl;
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

public class ClassAndMethodChangeListener extends PsiTreeChangeAdapter {
    private final Project project;
    private final TreeBuilder treeBuilder;
    private final CommentStatusCache commentCache;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassAndMethodChangeListener.class);


    public ClassAndMethodChangeListener(Project project, TreeBuilder treeBuilder, CommentStatusCache commentCache) {
        this.project = project;
        this.treeBuilder = treeBuilder;
        this.commentCache = commentCache;
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
        LOGGER.info("child removed - {}", eventChild.getClass());

        if (eventChild instanceof PsiMethodImpl) {
            removePsiElementFromTree(eventChild);
        } else if (eventChild instanceof PsiClassImpl) {
            removePsiElementFromTree(eventChild);
        }
    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {
        PsiElement element = event.getChild();
        LOGGER.info("child replaced - {}" , element.getClass());
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
            MethodNode newMethodNode = new MethodNode(method.getName(), method);
            parentNode.add(newMethodNode);
            treeBuilder.reload();
        } else {
            LOGGER.warn("Parent node not found for PSI element: {}", eventParent);
        }
    }

    private void addPsiClassToTree(PsiElement psiClassElement, PsiElement eventParent) {
        JTree tree = treeBuilder.getMethodTree();
        TreePath parentPath = PsiElementTreeUtil.findPathForPsiElement(tree, eventParent);
        if (parentPath == null) {
            LOGGER.info("parentPathIsNULL");
        } else {
            LOGGER.info(parentPath.toString());
        }
        if (parentPath != null) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
            PsiClass psiClass = (PsiClass) psiClassElement;
            ClassNode newClassNode = new ClassNode(psiClass.getName(), psiClass);
            parentNode.add(newClassNode);
            treeBuilder.reload();
        } else {
            LOGGER.warn("Parent node not found for PSI element: {}", eventParent);
        }
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
            }
        } else {
            LOGGER.warn("Node not found for PSI element: {}", psiElement);
        }
    }
}
