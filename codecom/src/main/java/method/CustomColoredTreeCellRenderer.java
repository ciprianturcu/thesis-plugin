package method;

import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import model.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class CustomColoredTreeCellRenderer extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (value instanceof AbstractTreeNode<?> treeNode) {
                SimpleTextAttributes attributes = getAttributes(treeNode);
                append(treeNode.getLabel(), attributes);
                setIcon(getIconFor(treeNode));
            }
            else {
                // Handle non-AbstractTreeNode objects generically
                append(value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                setIcon(AllIcons.Nodes.Unknown);
            }
    }

    private SimpleTextAttributes getAttributes(AbstractTreeNode<?> treeNode) {
        if (treeNode instanceof MethodNode methodNode) {
            return methodNode.hasDocComment() ?
                    new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GREEN) :
                    new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.RED);
        }
        return SimpleTextAttributes.REGULAR_ATTRIBUTES;
    }

    private Icon getIconFor(AbstractTreeNode<?> treeNode) {
        if (treeNode instanceof DirectoryNode) {
            return AllIcons.Nodes.Folder;
        } else if (treeNode instanceof ClassNode) {
            return AllIcons.Nodes.Class;
        } else if (treeNode instanceof MethodNode) {
            return AllIcons.Nodes.Method;
        } else {
            return AllIcons.Nodes.Unknown;
        }
    }
}
