package com.veridion.assignment.algolia;

import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;
import com.algolia.search.models.indexing.Query;
import com.veridion.assignment.model.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.List;

@Service
public class AlgoliaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlgoliaService.class);

    public AlgoliaService() {}

    private final SearchClient client = DefaultSearchClient.create("K7WMA52L67", "0f0a0719ba468a4e2f8ea68b43a288a5");
    private final SearchIndex<Company> index = client.initIndex("test_index", Company.class);

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
        return index.search(new Query(concatenateCompanyFields(company))).getHits();
    }

    public void deleteCompany(@Nonnull String objectID) {
        index.deleteObject(objectID);
    }

    public void deleteCompanies(@Nonnull List<String> objectIDs) {
        index.deleteObjects(objectIDs);
    }

    public void deleteAllEntries() {
        index.delete();
    }

    public String concatenateCompanyFields(Company company) {
        StringBuilder query = new StringBuilder();

        // Get all declared fields of the Company class
        Field[] fields = Company.class.getDeclaredFields();

        // Concatenate values of all fields separated by whitespace
        for (Field field : fields) {
            if (field.getName().equals("LOGGER")) {
                continue;
            }

            field.setAccessible(true); // Set accessible to true to access private fields if any
            try {
                Object value = field.get(company);
                if (value != null) {
                    query.append(value.toString());
                    query.append(" "); // Add whitespace separator
                }
            } catch (IllegalAccessException illegalAccessException) {
                LOGGER.error("An error has occurred while processing the company's fields: " + illegalAccessException.getMessage() + "."); // Handle the exception appropriately
            }
        }

        return query.toString().trim(); // Trim the trailing whitespace
    }
}
