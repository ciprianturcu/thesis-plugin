import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.CodeStyleManager;
import exceptions.ServerRequestException;
import httpclient.HttpClientPool;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import service.CommentService;
import settings.PluginSettings;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    @Mock
    private Project project;

    @Mock
    private Document document;

    @Mock
    private PsiMethod method;

    @Mock
    private HttpClientPool httpClientPool;

    @InjectMocks
    private CommentService commentService;

    private MockedStatic<WriteCommandAction> mockedStaticWriteCommandAction;
    private MockedStatic<ApplicationManager> mockedStaticApplicationManager;
    private MockedStatic<PluginSettings> mockedStaticPluginSettings;
    private MockedStatic<HttpClientPool> mockedStaticHttpClientPool;

    @BeforeEach
    void setUp() throws ServerRequestException {
        MockitoAnnotations.initMocks(this);

        // Mocking the Application class
        Application application = mock(Application.class);
        Disposable disposable = mock(Disposable.class);
        mockedStaticApplicationManager = mockStatic(ApplicationManager.class);
        mockedStaticApplicationManager.when(ApplicationManager::getApplication).thenReturn(application);
        ApplicationManager.setApplication(application, disposable);

        // Mocking the runReadAction method to return the expected method text
        when(application.runReadAction(any(Computable.class))).thenAnswer(invocation -> {
            Computable<String> computable = invocation.getArgument(0);
            return computable.compute();
        });

        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(application).invokeLater(any());

        // Mocking WriteCommandAction.runWriteCommandAction to execute the provided Runnable
        mockedStaticWriteCommandAction = mockStatic(WriteCommandAction.class);
        mockedStaticWriteCommandAction.when(() -> WriteCommandAction.runWriteCommandAction(any(Project.class), any(Runnable.class)))
                .thenAnswer(invocation -> {
                    Runnable runnable = invocation.getArgument(1);
                    runnable.run();
                    return null;
                });

        // Mocking PluginSettings
        PluginSettings pluginSettings = mock(PluginSettings.class);
        pluginSettings.serverUrl = "http://mockserver.com"; // Directly set the field
        mockedStaticPluginSettings = mockStatic(PluginSettings.class);
        mockedStaticPluginSettings.when(PluginSettings::getInstance).thenReturn(pluginSettings);

        // Mocking HttpClientPool
        mockedStaticHttpClientPool = mockStatic(HttpClientPool.class);
        mockedStaticHttpClientPool.when(HttpClientPool::getInstance).thenReturn(httpClientPool);

        // Initialize the httpClientPool fields
        when(httpClientPool.post(anyString(), anyString())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            String code = invocation.getArgument(1);
            // Return a mocked response based on the input
            if (url.contains("/getComment")) {
                return new JSONObject().put("summary_text", "This is a test method").toString();
            }
            return null;
        });
    }

    @Test
    void testGenerateCommentForMethod() throws ServerRequestException {
        String methodText = "public void test() {}";
        String sanitizedMethodText = "public void test() { }";
        String response = new JSONObject().put("summary_text", "This is a test method").toString();

        when(method.getText()).thenReturn(methodText);
        when(method.getTextRange()).thenReturn(new TextRange(0, methodText.length()));
        when(httpClientPool.post(anyString(), eq(sanitizedMethodText))).thenReturn(response);

        // Mock PsiDocumentManager and CodeStyleManager
        PsiDocumentManager psiDocumentManager = mock(PsiDocumentManager.class);
        when(PsiDocumentManager.getInstance(project)).thenReturn(psiDocumentManager);

        CodeStyleManager codeStyleManager = mock(CodeStyleManager.class);
        when(CodeStyleManager.getInstance(project)).thenReturn(codeStyleManager);

        commentService.generateCommentForMethod(project, document, method);

        verify(document).insertString(0, "/**\n* This is a test method\n*/");
        verify(psiDocumentManager).commitDocument(document);
        verify(codeStyleManager).reformat(method);
    }

    @Test
    void testGenerateCommentForMethod_throwsException() throws Exception {
        String methodText = "public void testMethod() {}";

        when(method.getText()).thenReturn(methodText);
        when(httpClientPool.post(any(String.class), any(String.class))).thenThrow(new ServerRequestException("Request failed"));

        assertThrows(ServerRequestException.class, () -> commentService.generateCommentForMethod(project, document, method));
        verify(httpClientPool, times(1)).post(any(String.class), eq(methodText));
    }

    @AfterEach
    void tearDown() {
        mockedStaticWriteCommandAction.close();
        mockedStaticApplicationManager.close();
        mockedStaticPluginSettings.close();
        mockedStaticHttpClientPool.close();
    }
}
