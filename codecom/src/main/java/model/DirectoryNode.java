package model;

import com.intellij.psi.PsiElement;

public class DirectoryNode extends AbstractTreeNode<PsiElement> {

    public DirectoryNode(String label, PsiElement psiElement) {
        super(label, psiElement);
    }

    @Override
    public String toString() {
        return label;
    }
}
