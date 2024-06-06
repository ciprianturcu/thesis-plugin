package service;

import com.intellij.execution.dashboard.actions.RunAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.javadoc.PsiDocComment;
import exceptions.ServerRequestException;
import httpclient.HttpClientPool;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.PropertyLoader;

import java.util.Properties;

public class CommentService {

    private static final String PROPERTIES_FILE = "service.properties";
    private static final String ENDPOINT_KEY = "comment-server-service-endpoint";
    private static final String RESOURCE_KEY;
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentService.class);

    static {
        Properties properties = PropertyLoader.loadProperties(PROPERTIES_FILE);
        String endpoint = properties.getProperty(ENDPOINT_KEY);
        RESOURCE_KEY = endpoint != null ? endpoint + "/getComment" : "";
    }

    private static volatile CommentService instance = new CommentService();

    private CommentService() {
    }

    public static CommentService getInstance() {
        if(instance == null)
        {
            synchronized (CommentService.class) {
                if(instance == null) {
                    instance = new CommentService();
                }
            }
        }
        return  instance;
    }

    public void generateCommentForMethod(Project project, Document document, PsiMethod method) throws ServerRequestException {
        String methodText = ApplicationManager.getApplication().runReadAction((Computable<String>) method::getText);

        String sanitizedMethodText = sanitizeMethodText(methodText);
        String result;
        result = getCommentResponse(sanitizedMethodText);

        if (result == null)
            throw new ServerRequestException("Server failed to generate a result. Try Again.");
        JSONObject codeComment = new JSONObject(result);
        String commentText = codeComment.getString("summary_text");
        PsiDocComment docCommentOfMethod = ApplicationManager.getApplication().runReadAction((Computable<PsiDocComment>) method::getDocComment);
        if (docCommentOfMethod == null) {
            int startOffset = method.getTextRange().getStartOffset();
            newComment(project, document, method, startOffset, commentText);
        } else {
            int startOffset = docCommentOfMethod.getTextOffset();
            int existingDocCommentLength = docCommentOfMethod.getTextLength();
            overrideComment(project, document, method, startOffset, existingDocCommentLength, commentText);
        }
    }

    private static void newComment(Project project, Document document, PsiMethod method, int startOffset, String commentText) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.insertString(startOffset, "/**\n* " + commentText + ".\n*/");
            PsiDocumentManager.getInstance(project).commitDocument(document);
            CodeStyleManager.getInstance(project).reformat(method);
        });
    }

    private static void overrideComment(Project project, Document document, PsiMethod method, int startOffset, int oldCommentLength, String commentText) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.deleteString(startOffset, startOffset + oldCommentLength);
            document.insertString(startOffset, "/**\n* " + commentText + ".\n*/");
            PsiDocumentManager.getInstance(project).commitDocument(document);
            CodeStyleManager.getInstance(project).reformat(method);
        });
    }

    private String sanitizeMethodText(String methodText) {
        return methodText.replaceAll("(?s)/\\*\\*.*?\\*/", "")
                .replace("\n", " ")
                .replace("\t", " ")
                .trim();
    }

    @Nullable
    private static String getCommentResponse(String trimmedMethod) throws ServerRequestException {
        String result;
        result = HttpClientPool.getInstance().post(RESOURCE_KEY, trimmedMethod);
        return result;
    }

}
