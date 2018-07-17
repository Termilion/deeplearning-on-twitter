package core;

import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.sentenceiterator.labelaware.LabelAwareSentenceIterator;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CustomLabelAwareFileSentenceIterator extends FileSentenceIterator implements LabelAwareSentenceIterator {
    /**
     * Takes a single file or directory
     * Label equals filename
     *
     * @param preProcessor the sentence pre processor
     * @param file         the file or folder to iterate over
     */
    public CustomLabelAwareFileSentenceIterator(SentencePreProcessor preProcessor, File file) {
        super(preProcessor, file);
    }

    public CustomLabelAwareFileSentenceIterator(File dir) {
        super(dir);
    }

    @Override
    public String currentLabel() {
        return currentFile.getName();
    }

    @Override
    public List<String> currentLabels() {
        return Arrays.asList(currentFile.getName());
    }
}
