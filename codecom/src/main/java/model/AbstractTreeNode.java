package model;

import com.intellij.psi.PsiElement;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class AbstractTreeNode<T extends PsiElement> extends DefaultMutableTreeNode {
    protected final T psiElement;

    public AbstractTreeNode( T psiElement) {
        this.psiElement = psiElement;
    }

    public abstract String getLabel();

    public T getPsiElement() {
        return psiElement;
    }

    @Override
    public abstract String toString();
}
