package utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiMethod;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static boolean hasComment(PsiMethod method) {
        final String[] methodText = new String[1];
        // Ensure that reading text from PsiMethod is done in a read action
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                 methodText[0] = method.getText();
            }
        });
        // Define the regex for extracting Javadoc comments
        String javadocRegex = "/\\*\\*.*?\\*/";
        Pattern pattern = Pattern.compile(javadocRegex, Pattern.DOTALL); // Enable dotall mode
        Matcher matcher = pattern.matcher(methodText[0]);

        if (matcher.find()) {
            //System.out.println("Comment was found." + matcher.group());
            return true;// Returns the first Javadoc comment found
        }
        return false;
    }

    public static boolean hasCommentV2(PsiMethod method) {
        return method.getDocComment() != null;
    }
}
