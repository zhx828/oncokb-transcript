package org.mskcc.oncokb.curation.importer;

import static org.mskcc.oncokb.curation.util.FileUtils.parseDelimitedFile;

import java.io.IOException;
import java.util.List;
import org.mskcc.oncokb.curation.config.application.ApplicationProperties;
import org.springframework.stereotype.Service;

@Service
public class CurationDataService {

    private final String DATA_DIRECTORY;
    private final String META_DATA_FOLDER_PATH;
    final ApplicationProperties applicationProperties;

    public CurationDataService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        DATA_DIRECTORY = applicationProperties.getOncokbDataRepoDir() + "/curation";
        META_DATA_FOLDER_PATH = DATA_DIRECTORY + "/meta";
    }

    public List<List<String>> parseTsvMetaFile(String fileName) throws IOException {
        return parseDelimitedFile(META_DATA_FOLDER_PATH + "/" + fileName, "\t", true);
    }

    public String getDataDirectory() {
        return DATA_DIRECTORY;
    }
}
