package io.github.binarybeing.hotcat.plugin.panel;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.ToolWindow;

import java.util.HashMap;

public class MethodTestGeneratorPanel implements Runnable{
    private ToolWindow toolWindow;
    private AnActionEvent event;
    int time = 0;
    public MethodTestGeneratorPanel(AnActionEvent event, ToolWindow toolWindow){
        this.toolWindow = toolWindow;
        this.event = event;
//        new HashMap<>().computeIfPresent("a")
    }

    @Override
    public void run() {
//        Javaassistor javaassistor = new Javaassistor();
//        DataContext dataContext = event.getDataContext();
//        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
//        if(!(psiFile instanceof PsiJavaFile)){
//            throw new RuntimeException("no selected java file founded");
//        }
//        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
//        if (editor == null) {
//            throw new RuntimeException("editor not found");
//        }
//        int position = editor.getSelectionModel().getSelectionStart();
//        try {
//            javaassistor.startMakingTest(event.getProject(), position, (PsiJavaFile) psiFile);
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }
}
