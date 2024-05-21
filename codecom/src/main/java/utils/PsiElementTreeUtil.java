package utils;
import com.intellij.psi.PsiElement;
import model.AbstractTreeNode;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class PsiElementTreeUtil {

    public static TreePath findPathForPsiElement(JTree tree, PsiElement targetElement) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        return findPathForPsiElement(tree, new TreePath(root), targetElement);
    }

    private static TreePath findPathForPsiElement(JTree tree, TreePath parent, PsiElement targetElement) {
        if (parent.getLastPathComponent() instanceof AbstractTreeNode<?> psiNode) {
            if (psiNode.getPsiElement().equals(targetElement)) {
                return parent;
            }

            if (psiNode.getChildCount() >= 0) {
                for (int i = 0; i < psiNode.getChildCount(); i++) {
                    AbstractTreeNode<?> childNode = (AbstractTreeNode<?>) psiNode.getChildAt(i);
                    TreePath path = parent.pathByAddingChild(childNode);
                    TreePath result = findPathForPsiElement(tree, path, targetElement);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }
}
