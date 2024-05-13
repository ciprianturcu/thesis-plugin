package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static utils.Utils.hasComment;
import static utils.Utils.hasCommentV2;

public class CommentAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if(editor == null ) return;

        Project project = event.getRequiredData(CommonDataKeys.PROJECT);
        Document document = editor.getDocument();

        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if(psiFile==null) return;

        SelectionModel selectionModel= editor.getSelectionModel();
        int selectionStart = selectionModel.getSelectionStart();

        PsiElement elementAtSelectionStart = psiFile.findElementAt(selectionStart);
        PsiMethod method = PsiTreeUtil.getParentOfType(elementAtSelectionStart, PsiMethod.class);
        assert method != null;
        System.out.println(hasComment(method));
        System.out.println(hasCommentV2(method));
        System.out.println(Objects.requireNonNull(method.getDocComment()).getText());
        System.out.println(method.getParent());
    }
}
