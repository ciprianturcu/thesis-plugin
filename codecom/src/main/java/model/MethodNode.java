package model;

import com.intellij.psi.PsiMethod;

public class MethodNode extends AbstractTreeNode<PsiMethod> {

    private boolean hasDocComment;

    public MethodNode(String label, PsiMethod psiMethod) {
        super(label, psiMethod);
        hasDocComment = updateCommentStatus();
    }

    public boolean hasDocComment() {
        return hasDocComment;
    }

    private  boolean updateCommentStatus() {
        return this.psiElement.getDocComment() != null;
    }

    @Override
    public String toString() {
        return label;
    }
}
