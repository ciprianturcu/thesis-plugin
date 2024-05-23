package model;

import com.intellij.psi.PsiClass;

public class ClassNode extends AbstractTreeNode<PsiClass> {

    public ClassNode( PsiClass psiElement) {
        super( psiElement);
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

