package method;

import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import model.TreeNodeData;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class CustomColoredTreeCellRenderer extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            // Based on the userObject type, you can decide how to render the node
            if (userObject instanceof TreeNodeData) {
                TreeNodeData nodeData = (TreeNodeData) userObject;
                append(nodeData.getLabel(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

                // Optionally set icon
                setIcon(getIconFor(nodeData.getType()));
            } else {
                append(value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        }
    }

    private Icon getIconFor(TreeNodeData.NodeType type) {
        // Return an icon based on the node type
        switch (type) {
            case DIRECTORY:
                return AllIcons.Nodes.Folder;
            case FILE:
                return AllIcons.FileTypes.Java;
            case CLASS:
                return AllIcons.Nodes.Class;
            case METHOD:
                return AllIcons.Nodes.Method;
            default:
                return AllIcons.Nodes.Unknown;
        }
    }
}
