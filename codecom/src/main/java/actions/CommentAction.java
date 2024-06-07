package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.CommentService;

public class CommentAction extends AnAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentAction.class);
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if(editor == null )
        {
            Messages.showMessageDialog("Please try again.", "Could Not Perform Action.", Messages.getErrorIcon());
            LOGGER.warn("The editor of this event could not be retrieved");
            return;
        }

        Project project = event.getRequiredData(CommonDataKeys.PROJECT);
        Document document = editor.getDocument();

        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if(psiFile==null) {
            Messages.showMessageDialog("Please try again.", "Could Not Perform Action.", Messages.getErrorIcon());
            LOGGER.warn("The file of this event could not be retrieved");
            return;
        }

        SelectionModel selectionModel= editor.getSelectionModel();
        int selectionStart = selectionModel.getSelectionStart();

        PsiElement elementAtSelectionStart = psiFile.findElementAt(selectionStart);
        PsiMethod method = PsiTreeUtil.getParentOfType(elementAtSelectionStart, PsiMethod.class);
        if(method == null) {
            Messages.showMessageDialog("No valid method was selected", "No Valid Method.", Messages.getWarningIcon());
            return;
        }
        try {
            // Perform the action
            CommentService.getInstance().generateCommentForMethod(project, document, method);
        } catch (Exception e) {
            // Handle the error and display the dialog on the EDT
            ApplicationManager.getApplication().invokeLater(() -> {
                // Show the error dialog
                showErrorDialog(e.getMessage());
            });
        }
    }

    private void showErrorDialog(String message) {
        // Implement your dialog displaying logic here
        Messages.showMessageDialog(message, "Error", Messages.getErrorIcon());
    }
}
