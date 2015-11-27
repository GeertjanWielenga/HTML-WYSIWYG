package org.netbeans.wysiwyg.editor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.HTMLEditor;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

@MultiViewElement.Registration(displayName = "Visual",
        mimeType = {"text/xhtml", "text/html"},
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "Visual",
        position = 1)
public class JavaFXMVE extends JPanel implements MultiViewElement, FileChangeListener {

    private FileObject obj;
    private JToolBar tb = new JToolBar();
    private static JFXPanel fxContainer;
    private HTMLEditor htmlEditor;

    public JavaFXMVE(Lookup lkp) {
        this.obj = lkp.lookup(FileObject.class);
        obj.addFileChangeListener(this);
        assert obj != null;
        fxContainer = new JFXPanel();
        Platform.setImplicitExit(false);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                createScene();
            }
        });
    }

    private void createScene() {
        htmlEditor = new HTMLEditor();
        try {
            htmlEditor.setHtmlText(obj.asText());
        } catch (IOException ex) {
            Logger.getLogger(JavaFXMVE.class.getName()).log(Level.SEVERE, null, ex);
        }
        StackPane root = new StackPane();
        root.getChildren().add(htmlEditor);
        fxContainer.setScene(new Scene(root));
    }

    @Override
    public JComponent getVisualRepresentation() {
        return fxContainer;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        tb.setFloatable(false);
        return tb;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return obj.getLookup();
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
    }

    @Override
    public void componentDeactivated() {
        FileLock fileLock = null;
        OutputStreamWriter osw;
        try {
            fileLock = obj.lock();
            OutputStream fout = obj.getOutputStream(fileLock);
            OutputStream bout = new BufferedOutputStream(fout);
            osw = new OutputStreamWriter(bout, "UTF-8");
            osw.write(htmlEditor.getHtmlText());
            osw.flush();
            osw.close();
        } catch (IOException ex) {
        } finally {
            if (fileLock != null) {
                fileLock.releaseLock();
            }
        }
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        callback.getTopComponent().setDisplayName(obj.getNameExt());
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
    }

    @Override
    public void fileChanged(final FileEvent fe) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    htmlEditor.setHtmlText(fe.getFile().asText());
                } catch (IOException ex) {
                }
            }
        });
    }

    @Override
    public void fileDeleted(FileEvent fe) {
    }

    @Override
    public void fileRenamed(FileRenameEvent fre) {
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fae) {
    }

}
