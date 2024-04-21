package model;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;

public class TreeNodeData {
    private final String label;
    private final NodeType type;
    private final PsiElement psiElement;

    public TreeNodeData(String label, NodeType type, PsiElement psiElement) {
        this.label = label;
        this.type = type;
        this.psiElement = psiElement;
    }

    public String getLabel() {
        return label;
    }

    public NodeType getType() {
        return type;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    @Override
    public String toString() {
        return label;
    }

    public enum NodeType {
        DIRECTORY, FILE, CLASS, METHOD
    }

}
