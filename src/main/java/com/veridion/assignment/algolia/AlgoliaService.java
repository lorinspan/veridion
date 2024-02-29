package com.veridion.assignment.algolia;

import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;
import com.algolia.search.models.indexing.Query;
import com.veridion.assignment.model.Company;
import com.veridion.assignment.model.CompanyUtil;
import com.veridion.assignment.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

@Service
public class AlgoliaService {
    private final SearchClient client = DefaultSearchClient.create("K7WMA52L67", "0f0a0719ba468a4e2f8ea68b43a288a5");
    private final SearchIndex<Company> index = client.initIndex("test_index", Company.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(AlgoliaService.class);

    public AlgoliaService() {}

    public void saveCompany(@Nonnull Company company) {
        index.saveObject(company, true);
    }

    public void saveCompanies(@Nonnull List<Company> companies) {
        index.saveObjects(companies, true);
    }

    public Company findCompany(Company company) {
        return findCompanies(company).get(0);
    }

    public List<Company> findCompanies(Company company) {
        return index.search(new Query(CompanyUtil.concatenateCompanyFields(company))).getHits();
    }

    public void deleteCompany(@Nonnull String objectID) {
        LOGGER.debug("Deleting companies with the object ID: " + objectID + ".");

        index.deleteObject(objectID);
    }

    public void deleteCompanies(@Nonnull List<String> objectIDs) {
        StringBuilder stringBuilder = new StringBuilder();
        objectIDs.forEach(objectID -> {
            stringBuilder.append(objectID).append(",");
        });

        LOGGER.debug("Deleting companies with the object IDs: " + Utils.removeLastCharacterIfExists(stringBuilder, ',') + ".");
        index.deleteObjects(objectIDs);
    }


    public void deleteAllEntries() {
        LOGGER.warn("DELETING ALL ENTRIES!");
        index.delete();
    }
}
