package actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import model.AbstractTreeNode;
import model.MethodNode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.CommentService;

import javax.swing.*;

public class CommentNodeAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommentNodeAction.class);

    public static void performActionOnSelectedMethod(Project project , JTree methodTree) {
        // Get the selected node from the tree
        AbstractTreeNode<?> selectedNode = (AbstractTreeNode<?>) methodTree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            // Check if it is a method node before performing the action
            if (selectedNode instanceof MethodNode methodNode) {
                PsiFile psiFile = methodNode.getPsiElement().getContainingFile();
                if (psiFile == null) {
                    Messages.showMessageDialog("Please try again.", "Could Not Perform Action.", Messages.getErrorIcon());
                    LOGGER.warn("The file of this event could not be retrieved");
                    return;
                }

                Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                if (document == null) {
                    Messages.showMessageDialog("Please try again.", "Could Not Perform Action.", Messages.getErrorIcon());
                    LOGGER.warn("The document for this file could not be retrieved");
                    return;
                }
                if (methodNode.hasDocComment()) {
                    // Show confirmation dialog
                    int response = JOptionPane.showConfirmDialog(null,
                            "The method already has a documentation comment. Do you want to overwrite it?",
                            "Confirm Action",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (response != JOptionPane.OK_OPTION) {
                        // User selected cancel or closed the dialog, so stop the action
                        return;
                    }
                }

                // Run the task with a progress bar
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generating comment", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        // Set progress bar properties
                        indicator.setIndeterminate(true);

                        // Perform the action
                        try {
                            // Perform the action
                            CommentService.getInstance().generateCommentForMethod(project, document, methodNode.getPsiElement());
                        } catch (Exception e) {
                            // Handle the error and display the dialog on the EDT
                            ApplicationManager.getApplication().invokeLater(() -> {
                                // Show the error dialog
                                showErrorDialog(e.getMessage());
                            });
                        }
                    }
                });
            }
            else {
                Messages.showMessageDialog("Wrong node type selected! Please select a method node.", "Could Not Perform Action.", Messages.getErrorIcon());
                LOGGER.warn("No node was selected");
            }
        }
        else {
            Messages.showMessageDialog("No node selected! Please select a method node.", "Could Not Perform Action.", Messages.getErrorIcon());
            LOGGER.warn("Wrong type of node was selected");
        }
    }
    private static void showErrorDialog(String message) {
        // Implement your dialog displaying logic here
        Messages.showMessageDialog(message, "Error", Messages.getErrorIcon());
    }
}
