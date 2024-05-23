package method;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.List;

public class SortedTreeModel extends DefaultTreeModel {

    public SortedTreeModel(TreeNode root) {
        super(root);
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent instanceof DefaultMutableTreeNode treeNode) {
            List<TreeNode> children = getSortedChildren(treeNode);
            return children.get(index);
        }
        return super.getChild(parent, index);
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent instanceof DefaultMutableTreeNode treeNode) {
            return getSortedChildren(treeNode).size();
        }
        return super.getChildCount(parent);
    }

    private List<TreeNode> getSortedChildren(DefaultMutableTreeNode parent) {
        List<TreeNode> children = Collections.list(parent.children());
        children.sort((node1, node2) -> {
            String name1 = node1.toString();
            String name2 = node2.toString();
            return name1.compareToIgnoreCase(name2);
        });
        return children;
    }
}

