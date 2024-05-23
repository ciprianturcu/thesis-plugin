package utils;
import com.intellij.psi.PsiElement;
import model.AbstractTreeNode;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PsiElementTreeUtil {

    public static TreePath findPathForPsiElement(JTree tree, PsiElement targetElement) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        return findPathForPsiElement(new TreePath(root), targetElement);
    }

    private static TreePath findPathForPsiElement(TreePath parent, PsiElement targetElement) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();

        if (node instanceof AbstractTreeNode<?> psiNode) {
            if (psiNode.getPsiElement().equals(targetElement)) {
                return parent;
            }
        }

        if (node.getChildCount() >= 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                TreePath path = parent.pathByAddingChild(childNode);
                TreePath result = findPathForPsiElement(path, targetElement);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    // Method to save the expansion state of the tree
    public static List<TreePath> saveExpansionState(JTree tree) {
        List<TreePath> expandedPaths = new ArrayList<>();
        Enumeration<?> enumeration = tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                expandedPaths.add((TreePath) enumeration.nextElement());
            }
        }
        return expandedPaths;
    }

    // Method to restore the expansion state of the tree
    public static void restoreExpansionState(JTree tree, List<TreePath> expandedPaths) {
        for (TreePath path : expandedPaths) {
            tree.expandPath(path);
        }
    }

    // Method to repaint the tree and keep it expanded
    public static void repaintTreeAndKeepExpanded(JTree tree) {
        // Save the current expansion state
        List<TreePath> expandedPaths = saveExpansionState(tree);

        // Repaint the tree or update its model here
        // Example: tree.setModel(newModel); or tree.repaint();

        // Restore the expansion state
        restoreExpansionState(tree, expandedPaths);
    }
}
