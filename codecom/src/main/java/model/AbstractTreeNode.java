package model;

import com.intellij.psi.PsiElement;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class AbstractTreeNode<T extends PsiElement> extends DefaultMutableTreeNode {
    protected final String label;
    protected final T psiElement;

    public AbstractTreeNode(String label, T psiElement) {
        this.label = label;
        this.psiElement = psiElement;
    }

    public String getLabel() {
        return label;
    }

    public T getPsiElement() {
        return psiElement;
    }

    @Override
    public abstract String toString();
}
