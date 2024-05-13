package model;

import com.intellij.psi.PsiClass;

public class ClassNode extends AbstractTreeNode<PsiClass> {

    public ClassNode(String label, PsiClass psiElement) {
        super(label, psiElement);
    }

    @Override
    public String toString() {
        return label;
    }
}

