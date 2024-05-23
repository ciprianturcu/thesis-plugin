package model;

import com.intellij.psi.PsiDirectory;

public class DirectoryNode extends AbstractTreeNode<PsiDirectory> {

    public DirectoryNode(PsiDirectory psiDirectory) {
        super(psiDirectory);
    }

    @Override
    public String getLabel() {
        return psiElement.getName();
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
