/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.modeshape.jcr.query.lucene;

import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Weight;
import org.modeshape.jcr.query.lucene.CaseOperations.CaseOperation;
import org.modeshape.jcr.value.Path;
import org.modeshape.jcr.value.ValueComparators;
import org.modeshape.jcr.value.ValueFactories;
import org.modeshape.jcr.value.ValueFactory;

/**
 * A Lucene {@link Query} implementation that is used to apply a {@link javax.jcr.query.qom.Comparison} constraint against the
 * Path of nodes. This query implementation works by using the weight and {@link Weight#scorer(IndexReader, boolean, boolean)
 * scorer} of the wrapped query to score (and return) only those documents that correspond to nodes with Paths that satisfy the
 * constraint.
 */
public class ComparePathQuery extends CompareQuery<Path> {

    private static final long serialVersionUID = 1L;
    protected static final Evaluator<Path> PATH_IS_LESS_THAN = new Evaluator<Path>() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean satisfiesConstraint( Path nodePath,
                                            Path constraintPath ) {
            return ValueComparators.PATH_COMPARATOR.compare(nodePath, constraintPath) < 0;
        }

        @Override
        public String toString() {
            return " < ";
        }
    };
    protected static final Evaluator<Path> PATH_IS_LESS_THAN_OR_EQUAL_TO = new Evaluator<Path>() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean satisfiesConstraint( Path nodePath,
                                            Path constraintPath ) {
            return ValueComparators.PATH_COMPARATOR.compare(nodePath, constraintPath) <= 0;
        }

        @Override
        public String toString() {
            return " <= ";
        }
    };
    protected static final Evaluator<Path> PATH_IS_GREATER_THAN = new Evaluator<Path>() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean satisfiesConstraint( Path nodePath,
                                            Path constraintPath ) {
            return ValueComparators.PATH_COMPARATOR.compare(nodePath, constraintPath) > 0;
        }

        @Override
        public String toString() {
            return " > ";
        }
    };
    protected static final Evaluator<Path> PATH_IS_GREATER_THAN_OR_EQUAL_TO = new Evaluator<Path>() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean satisfiesConstraint( Path nodePath,
                                            Path constraintPath ) {
            return ValueComparators.PATH_COMPARATOR.compare(nodePath, constraintPath) >= 0;
        }

        @Override
        public String toString() {
            return " >= ";
        }
    };

    /**
     * Construct a {@link Query} implementation that scores documents such that the node represented by the document has a path
     * that is greater than the supplied constraint path.
     * 
     * @param constraintPath the constraint path; may not be null
     * @param fieldName the name of the document field containing the path value; may not be null
     * @param factories the value factories that can be used during the scoring; may not be null
     * @param caseOperation the operation that should be performed on the indexed values before the constraint value is being
     *        evaluated; may not be null
     * @return the path query; never null
     */
    public static ComparePathQuery createQueryForNodesWithPathGreaterThan( Path constraintPath,
                                                                           String fieldName,
                                                                           ValueFactories factories,
                                                                           CaseOperation caseOperation ) {
        return new ComparePathQuery(fieldName, constraintPath, factories.getPathFactory(), factories.getStringFactory(),
                                    PATH_IS_GREATER_THAN, caseOperation);
    }

    /**
     * Construct a {@link Query} implementation that scores documents such that the node represented by the document has a path
     * that is greater than or equal to the supplied constraint path.
     * 
     * @param constraintPath the constraint path; may not be null
     * @param fieldName the name of the document field containing the path value; may not be null
     * @param factories the value factories that can be used during the scoring; may not be null
     * @param caseOperation the operation that should be performed on the indexed values before the constraint value is being
     *        evaluated; may not be null
     * @return the path query; never null
     */
    public static ComparePathQuery createQueryForNodesWithPathGreaterThanOrEqualTo( Path constraintPath,
                                                                                    String fieldName,
                                                                                    ValueFactories factories,
                                                                                    CaseOperation caseOperation ) {
        return new ComparePathQuery(fieldName, constraintPath, factories.getPathFactory(), factories.getStringFactory(),
                                    PATH_IS_GREATER_THAN_OR_EQUAL_TO, caseOperation);
    }

    /**
     * Construct a {@link Query} implementation that scores documents such that the node represented by the document has a path
     * that is less than the supplied constraint path.
     * 
     * @param constraintPath the constraint path; may not be null
     * @param fieldName the name of the document field containing the path value; may not be null
     * @param factories the value factories that can be used during the scoring; may not be null
     * @param caseOperation the operation that should be performed on the indexed values before the constraint value is being
     *        evaluated; may not be null
     * @return the path query; never null
     */
    public static ComparePathQuery createQueryForNodesWithPathLessThan( Path constraintPath,
                                                                        String fieldName,
                                                                        ValueFactories factories,
                                                                        CaseOperation caseOperation ) {
        return new ComparePathQuery(fieldName, constraintPath, factories.getPathFactory(), factories.getStringFactory(),
                                    PATH_IS_LESS_THAN, caseOperation);
    }

    /**
     * Construct a {@link Query} implementation that scores documents such that the node represented by the document has a path
     * that is less than or equal to the supplied constraint path.
     * 
     * @param constraintPath the constraint path; may not be null
     * @param fieldName the name of the document field containing the path value; may not be null
     * @param factories the value factories that can be used during the scoring; may not be null
     * @param caseOperation the operation that should be performed on the indexed values before the constraint value is being
     *        evaluated; may not be null
     * @return the path query; never null
     */
    public static ComparePathQuery createQueryForNodesWithPathLessThanOrEqualTo( Path constraintPath,
                                                                                 String fieldName,
                                                                                 ValueFactories factories,
                                                                                 CaseOperation caseOperation ) {
        return new ComparePathQuery(fieldName, constraintPath, factories.getPathFactory(), factories.getStringFactory(),
                                    PATH_IS_LESS_THAN_OR_EQUAL_TO, caseOperation);
    }

    private final CaseOperation caseOperation;

    /**
     * Construct a {@link Query} implementation that scores nodes according to the supplied comparator.
     * 
     * @param fieldName the name of the document field containing the path value; may not be null
     * @param constraintPath the constraint path; may not be null
     * @param pathFactory the value factory that can be used during the scoring; may not be null
     * @param stringFactory the string factory that can be used during the scoring; may not be null
     * @param evaluator the {@link CompareQuery.Evaluator} implementation that returns whether the node path satisfies the
     *        constraint; may not be null
     * @param caseOperation the operation that should be performed on the indexed values before the constraint value is being
     *        evaluated; may not be null
     */
    protected ComparePathQuery( String fieldName,
                                Path constraintPath,
                                ValueFactory<Path> pathFactory,
                                ValueFactory<String> stringFactory,
                                Evaluator<Path> evaluator,
                                CaseOperation caseOperation ) {
        super(fieldName, constraintPath, pathFactory, stringFactory, evaluator);
        this.caseOperation = caseOperation;
    }

    @Override
    protected Path readFromDocument( IndexReader reader,
                                     int docId ) throws IOException {
        Document doc = reader.document(docId, fieldSelector);
        String valueString = doc.get(fieldName);
        if (valueString == null) return null;
        valueString = caseOperation.execute(valueString);
        return valueTypeFactory.create(valueString);
    }

    @Override
    public Object clone() {
        return new ComparePathQuery(fieldName, constraintValue, valueTypeFactory, stringFactory, evaluator, caseOperation);
    }
}
