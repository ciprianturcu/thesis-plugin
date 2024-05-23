package model;

import com.intellij.psi.PsiMethod;

public class MethodNode extends AbstractTreeNode<PsiMethod> {

    private boolean hasDocComment;

    public MethodNode(PsiMethod psiMethod) {
        super(psiMethod);
        hasDocComment = updateCommentStatus();
    }

    public boolean hasDocComment() {
        return hasDocComment;
    }

    private void refreshCommentStatus() {
        hasDocComment = updateCommentStatus();
    }

    private boolean updateCommentStatus() {
        return this.psiElement.getDocComment() != null;
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
