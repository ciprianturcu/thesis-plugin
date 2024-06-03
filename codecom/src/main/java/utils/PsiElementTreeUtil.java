package utils;
import com.intellij.psi.PsiElement;
import listeners.ClassAndMethodChangeListener;
import model.AbstractTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PsiElementTreeUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PsiElementTreeUtil.class);

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

        long startTime = System.nanoTime();  // Start timing


        // Save the current expansion state
        List<TreePath> expandedPaths = saveExpansionState(tree);

        tree.repaint();

        // Restore the expansion state
        restoreExpansionState(tree, expandedPaths);
        long endTime = System.nanoTime();  // End timing
        long duration = endTime - startTime;  // Calculate duration in nanoseconds

        LOGGER.info("Tree repaint took {} ms", duration / 1_000_000.0);
    }
}
