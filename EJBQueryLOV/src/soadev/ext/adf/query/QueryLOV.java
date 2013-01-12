package soadev.ext.adf.query;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;

import oracle.adf.model.BindingContext;
import oracle.adf.model.bean.DCDataRow;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.input.RichInputListOfValues;
import oracle.adf.view.rich.event.LaunchPopupEvent;
import oracle.adf.view.rich.event.QueryEvent;
import oracle.adf.view.rich.model.AttributeCriterion;
import oracle.adf.view.rich.model.AttributeDescriptor;
import oracle.adf.view.rich.model.ColumnDescriptor;
import oracle.adf.view.rich.model.ConjunctionCriterion;
import oracle.adf.view.rich.model.Criterion;
import oracle.adf.view.rich.model.FilterableQueryDescriptor;
import oracle.adf.view.rich.model.ListOfValuesModel;
import oracle.adf.view.rich.model.QueryDescriptor;
import oracle.adf.view.rich.model.QueryModel;
import oracle.adf.view.rich.model.TableModel;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import oracle.jbo.uicli.binding.JUCtrlHierBinding;
import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;
import oracle.jbo.uicli.binding.JUCtrlHierTypeBinding;

import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.queries.FetchGroup;

import soadev.ext.adf.BeanModelXmlParser;
import static soadev.ext.adf.query.Constants.BLANK;
import static soadev.ext.adf.query.Constants.BLANK_ENDING_DATE;
import static soadev.ext.adf.query.Constants.BLANK_STARTING_DATE;
import static soadev.ext.adf.query.Constants.CLOSE_BRACKET;
import static soadev.ext.adf.query.Constants.COMPONENT_MAP;
import static soadev.ext.adf.query.Constants.DATE_TYPE;
import static soadev.ext.adf.query.Constants.DOT;
import static soadev.ext.adf.query.Constants.OPEN_BRACKET;
import static soadev.ext.adf.query.Constants.SELECT;
import static soadev.ext.adf.query.Constants.SPACE;
import static soadev.ext.adf.query.Constants.STRING_TYPE;
import static soadev.ext.adf.query.Constants.TIMESTAMP_TYPE;
import static soadev.ext.adf.query.Constants._AND;
import static soadev.ext.adf.query.Constants._FROM_;
import static soadev.ext.adf.query.Constants._IS_NOT_NULL;
import static soadev.ext.adf.query.Constants._IS_NULL;
import static soadev.ext.adf.query.Constants._OBJECT_ALIAS;
import static soadev.ext.adf.query.Constants._OR;
import static soadev.ext.adf.query.Constants._UPPER_FUNCTION;
import static soadev.ext.adf.query.Constants._WHERE_CLAUSE;

import soadev.pattern.util.Observer;
import soadev.pattern.util.Subject;

import soadev.utils.MyUtils;
import static soadev.utils.MyUtils.unbox;

import soadev.view.utils.JSFUtils;


/**
 * A reusable bean implementation for the query and quickQuery components.
 * @author rommel pino http://soadev.blogspot.com
 */
public class QueryLOV implements Subject, Serializable{
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(QueryLOV.class);
    private static final long serialVersionUID = 1L;
    private String _queryId;
    private transient DummyMap _param;
    private transient String _entityName;
    private transient String[] accessorNames;
    private String _subProperty;
    private transient String _jpql;
    private transient String _operName;
    private boolean hasInitialized = false;
    private transient Map<String, AttributeDef> _attributes;
    private transient List<AttributeDef> _attributeList;
    private transient QueryDescriptor _currentDescriptor;
    private transient QueryModel _queryModel;
    private transient ListOfValuesModel _listOfValuesModel;
    private transient String _searchCriteria;
    private String _parameter;
    private List<SavedSearchDef> _systemSavedSearchDefList;
    private transient List<SavedSearchDef> _savedSearchDefList;
    private transient Map<String, QueryDescriptor> _queryDescriptorMap;
    //provide managed property to support persistence
    private AbstractQueryPersistenceHelper _queryPersistenceHelper;
    private transient Map<String, Object> _recentSelections; //for easy used by converter
    private transient DummyMap _convert;
    private transient AttributeDef _keyAttrDef;
    private String _converterKey;
    private transient Converter _dynamicConverter;
    private boolean _showAllRecordsByDefaultForNonAutoExecuteQD = false;
    private boolean _firstCallToGetJpql = true;
    //TODO how will a managed property be reinjected?
    private transient Map<String, Object> _supplementaryMap;
    private transient String _selectClause;
    private transient List<Object[]> _queryHints;
    private transient String _jpqlStmt;
    //pagination support
    private List<Observer> _observers;

    //no-arg constructor to support declaration as managed bean

    public QueryLOV() {
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        hasInitialized = false;
    }
    
    public void prepare(){
        _jpql = null; //so jpql will be reinitialize on next call to getJpql()
        _jpqlStmt = null;
        notifyObservers();
    }

    public AbstractQueryPersistenceHelper getQueryPersistenceHelper() {
        return _queryPersistenceHelper;
    }

    private Map<String, QueryDescriptor> getQueries() {
        if (_queryDescriptorMap == null) {
            _queryDescriptorMap = new TreeMap<String, QueryDescriptor>();
            for (SavedSearchDef searchDef : getSavedSearchDefList()) {
                QueryDescriptorImpl qd = new QueryDescriptorImpl(searchDef);
                _queryDescriptorMap.put(searchDef.getName(), qd);
            }
        }
        return _queryDescriptorMap;
    }


    private void setup() {
        if (_parameter == null) {
            throw new IllegalStateException("Parameter like [bindings.Entity] not yet set");
        }
        setupAttributes();
        setupSavedSearchDefs();
        hasInitialized = true;
    }

    private void setupSavedSearchDefs() {
        addAllSearchDefList(getSystemSavedSearchDefList());
        addAllSearchDefList(getUserSavedSearchDefList());
    }

    private List<SavedSearchDef> getUserSavedSearchDefList() {
        AbstractQueryPersistenceHelper helper = getQueryPersistenceHelper();
        if (helper != null) {
            return helper.findSavedSearchDefByQueryId(getQueryId());
        }
        return Collections.emptyList();
    }

    private String buildAttributeExpression(String attrName, String type) {
        StringBuilder builder = new StringBuilder();
        if (type.equals(STRING_TYPE)) { //can support case insensitive search
            builder.append(_UPPER_FUNCTION);
            builder.append(OPEN_BRACKET);
            builder.append(_OBJECT_ALIAS);
            builder.append(DOT);
            builder.append(attrName);
            builder.append(CLOSE_BRACKET);
        } else {
            builder.append(_OBJECT_ALIAS);
            builder.append(DOT);
            builder.append(attrName);
        }
        return builder.toString();
    }

    public String formatStringForJPQL(String value) {
        StringBuilder builder = new StringBuilder();
        builder.append("'");
        builder.append(getValueString(value));
        builder.append("'");
        return builder.toString();
    }


    private String escape(String value) {
        return value.replace("'", "''");
    }

    private String getValueString(Object value) {
        return escape(value.toString()).toUpperCase(); //toUpperCase to support Case Insensitive search
    }

    public QueryDescriptor getCurrentDescriptor() {
        if (_currentDescriptor == null) {
            if (!hasInitialized) {
                setup();
            }
            Map<String, QueryDescriptor> qdMap = getQueries();
            for (Map.Entry<String, QueryDescriptor> entry : qdMap.entrySet()) {
                QueryDescriptor qd = entry.getValue();
                if (unbox((Boolean)qd.getUIHints().get(QueryDescriptor.UIHINT_DEFAULT))) {
                    _currentDescriptor = qd;
                }
            }
            if (_currentDescriptor == null && !qdMap.isEmpty()) {
                for (Map.Entry<String, QueryDescriptor> entry :
                     qdMap.entrySet()) {
                    //mark first as default
                    _currentDescriptor = entry.getValue();
                    _currentDescriptor.getUIHints().put(QueryDescriptor.UIHINT_DEFAULT,
                                                        true);
                }
            }
            //if no provided savedsearch then generate a default
            if (_currentDescriptor == null && !getAttributeList().isEmpty()) {
                SavedSearchDef ssd = new SavedSearchDef();
                ssd.setQuery(this);
                ssd.setName("System Generated");
                ssd.setReadOnly(true);
                ssd.setDefaultSearch(true);
                SearchFieldDef sfd = new SearchFieldDef();
                sfd.setAttrName(getAttributeList().get(0).getName());
                ssd.addSearchFieldDef(sfd);
                QueryDescriptor qd = new QueryDescriptorImpl(ssd);
                getQueries().put(ssd.getName(), qd);
                _currentDescriptor = qd;
            }
        }
        return _currentDescriptor;
    }

    public void setCurrentDescriptor(QueryDescriptor descriptor) {
        this._currentDescriptor = descriptor;
    }

    public QueryModel getQueryModel() {
        if (_queryModel == null) {
            if (!hasInitialized) {
                setup();
            }
            //initialize queryDescriptors
            getQueries();
            _queryModel = new QueryModelImpl();
        }
        return _queryModel;
    }

    public ListOfValuesModel getListOfValuesModel() {
        if (_listOfValuesModel == null) {
            if (!hasInitialized) {
                setup();
            }
            _listOfValuesModel = new ListOfValuesModelImpl();
        }
        return _listOfValuesModel;
    }

    private void addAttributeDefs(List<AttributeDef> attributeDefs) {
        //put in list
        getAttributeList().addAll(attributeDefs);
        //put in map
        for (AttributeDef attrDef : attributeDefs) {
            getAttributes().put(attrDef.getName(), attrDef);
        }
    }

    private String getSearchCriteria() {
        _logger.fine("QueryLOV.getSearchCriteria begin" + _searchCriteria + _firstCallToGetJpql);
        //establish search criteria for auto execute QDs on first call
        //let the query operation listener take charge of the subsequent call
        if (_searchCriteria == null && _firstCallToGetJpql) {
            _firstCallToGetJpql = false;
            QueryDescriptor qd = getCurrentDescriptor();
            //set search criteria if qd is autoExecute
            if (unbox((Boolean)qd.getUIHints().get(QueryDescriptor.UIHINT_AUTO_EXECUTE))) {
                _searchCriteria = qd.toString();
            }
            notifyObservers();
        }
        return _searchCriteria;
    }

    public void processQuery(QueryEvent event) {
        _logger.fine("QueryLOV.processQuery begin");
        QueryDescriptorImpl descriptor =
            (QueryDescriptorImpl)event.getDescriptor();
        String criteria = descriptor.toString();
        setSearchCriteria(criteria);
        prepare();
    }
    @Deprecated
    public String getJpql() {
        if (_jpql == null) { //to avoid rebuilding of jpql if not necessary
            String searchCriteria = getSearchCriteria();
            //showing all records by default can be turned-off
            //by providing a managed property 'showAllRecordsByDefault' with value 'false'
            if (isShowAllRecordsByDefault() || searchCriteria != null) {
                StringBuilder builder = new StringBuilder();
                builder.append(getSelectClause());
                builder.append(getJoinFetchClause());
                if (searchCriteria != null && !"".equals(searchCriteria)) {
                    builder.append(_WHERE_CLAUSE).append(OPEN_BRACKET).append(searchCriteria).append(CLOSE_BRACKET);
                }
                _jpql = builder.toString();
            }
        }
        return _jpql;
    }
    
    public String getJpqlStmt(){
        if(_jpqlStmt == null){
            String searchCriteria = getSearchCriteria();
            //showing all records by default can be turned-off
            //by providing a managed property 'showAllRecordsByDefault' with value 'false'
            if (isShowAllRecordsByDefault() || searchCriteria != null) {
                StringBuilder builder = new StringBuilder();
                builder.append(getSelectClause());
                if (searchCriteria != null && !"".equals(searchCriteria)) {
                    builder.append(_WHERE_CLAUSE).append(OPEN_BRACKET).append(searchCriteria).append(CLOSE_BRACKET);
                }
                _jpqlStmt = builder.toString();
            }
        }
        _logger.fine("QueryLOV.getJpqlStmt : " + _jpqlStmt );
        return _jpqlStmt;
    }

    public List<Object[]> getHints() {
        if (_queryHints == null) {
            _queryHints = new ArrayList<Object[]>();
            if (accessorNames != null && accessorNames.length > 0) {
                for (String value : accessorNames) {
                    _queryHints.add(new Object[]{QueryHints.LEFT_FETCH, buildJPQLPropertyStr(value)});
                    _logger.fine("QueryLOV.getQueryHints accessor name: " + value );
                }
            }
            _queryHints.add( new Object[]{QueryHints.FETCH_GROUP, getFetchGroup()});
        }
        return _queryHints;
    }

    private String buildJPQLPropertyStr(String value) {
        StringBuilder builder = new StringBuilder();
        builder.append(_OBJECT_ALIAS).append(DOT).append(value);
        return builder.toString();
    }

    private FetchGroup getFetchGroup() {
        FetchGroup group = new FetchGroup();
        for (AttributeDef attrDef : getAttributeList()) {
            if(attrDef.isQueriable()){
                group.addAttribute(attrDef.getName());
            }
        }
        return group;
    }

    private String getSelectClause() {
        if (_selectClause == null) {
            StringBuilder builder = new StringBuilder();
            builder.append(SELECT);
            builder.append(_OBJECT_ALIAS);
            builder.append(_FROM_);
            builder.append(getEntityName());
            builder.append(_OBJECT_ALIAS);
            _selectClause = builder.toString();
        }
        return _selectClause;
    }

    private String getJoinFetchClause() {
        if (accessorNames != null && accessorNames.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (String value : accessorNames) {
                builder.append(" LEFT JOIN FETCH");
                builder.append(_OBJECT_ALIAS);
                builder.append(DOT);
                builder.append(value);
            }
            return builder.toString();
        }
        return "";
    }


    private JUCtrlHierBinding getTreeBinding() {
        String treeBindingExpression = createExpression(_parameter);
        return (JUCtrlHierBinding)JSFUtils.resolveExpression(treeBindingExpression);
    }

    private CollectionModel getInternalCollectionModel() {
        String collectionModelExpression =
            createExpression(_parameter + ".collectionModel");
        return (CollectionModel)JSFUtils.resolveExpression(collectionModelExpression);
    }

    private void setupAttributes() {
        JUCtrlHierBinding juchb = getTreeBinding();
        setupEntityName(juchb);
        setupOperationBindingName(juchb);
        addAttributeDefs(getPageDefAttributes(juchb));
    }

    private void setupEntityName(JUCtrlHierBinding juchb) {
        JUCtrlHierTypeBinding[] typeBindings = juchb.getTypeBindings();
        String fullEntityName = typeBindings[0].getStructureDefName();
        _entityName = getShortName(fullEntityName);
    }

    private void setupOperationBindingName(JUCtrlHierBinding juchb) {
        DCIteratorBinding iter = juchb.getIteratorBinding();
        String sourceName = iter.getSourceName();
        String[] tokens = sourceName.split("[_]");
        String operName = tokens[tokens.length - 2];
        _operName = operName;

    }

    private String getShortName(String fullEntityName) {
        String[] tokens = fullEntityName.split("[.]");
        return tokens[tokens.length - 1];
    }


    private List<AttributeDef> getPageDefAttributes(JUCtrlHierBinding juchb) {
        JUCtrlHierTypeBinding[] typeBindings = juchb.getTypeBindings();
        DCIteratorBinding iter = juchb.getDCIteratorBinding();
        List<AttributeDef> pageDefAttributes = new ArrayList<AttributeDef>();
        //the first typeBinding should be the root entity
        accessorNames = typeBindings[0].getAccessorNames();
        String[] attributes = typeBindings[0].getAttrNames();
        String entitySDef = typeBindings[0].getStructureDefName();
        Map<String, AttributeDef> entityAttributesMap =
            BeanModelXmlParser.getAttributeDefsMap(getFilePath(entitySDef));
        for (String attr : attributes) {
            //attrDef should not be null always
            AttributeDef attrDef = entityAttributesMap.get(attr);
            pageDefAttributes.add(attrDef);
        }

        //supports only first level accessors
        //assumes that the sequence of the accessorNames
        //is the same as the accessor definitions
        for (int i = 1, j = 0; i < typeBindings.length; i++, j++) {
            JUCtrlHierTypeBinding jhtb = typeBindings[i];
            if (jhtb == null){
                throw new IllegalArgumentException("JUCtrlHierTypeBinding with index +" + i +" is null. AccessorName: " + accessorNames[j]);
            }
            String[] accessorAttributes = jhtb.getAttrNames();
            String accessorName = accessorNames[j];
            String accessorSDef = jhtb.getStructureDefName();
            Map<String, AttributeDef> accessorAttributesMap =
                BeanModelXmlParser.getAttributeDefsMap(getFilePath(accessorSDef));
            for (String attr : accessorAttributes) {
                AttributeDef attrDef = accessorAttributesMap.get(attr);
                attrDef.setName(AttributeDef.concatWithDot(accessorName,
                                                           attr));
                attrDef.setAccessorName(accessorName);
                pageDefAttributes.add(attrDef);
            }
        }
        return pageDefAttributes;
    }


    public String getEntityName() {
        return _entityName;
    }

    public void setSearchCriteria(String searchCriteria) {
        this._searchCriteria = searchCriteria;
    }

    public List<SavedSearchDef> getSavedSearchDefList() {
        if (_savedSearchDefList == null) {
            _savedSearchDefList = new ArrayList<SavedSearchDef>();
        }
        return _savedSearchDefList;
    }

    public void setAttributes(Map<String, AttributeDef> attributes) {
        this._attributes = attributes;
    }

    public Map<String, AttributeDef> getAttributes() {
        if (_attributes == null) {
            _attributes = new LinkedHashMap<String, AttributeDef>();
        }
        return _attributes;
    }

    public AttributeDef getAttributeDescriptor(String attrName) {
        if (_attributes != null)
            return _attributes.get(attrName);

        return null;
    }


    private String getFilePath(String entityName) {
        entityName = entityName.replace('.', '/');
        entityName = entityName.concat(".xml");
        return entityName;
    }

    private static String createExpression(String value) {
        StringBuilder builder = new StringBuilder();
        builder.append("#{");
        builder.append(value);
        builder.append("}");
        return builder.toString();
    }

    public DummyMap getParam() {
        if (_param == null) {
            _param = new DummyMap() {
                    public Object get(Object obj) {
                        if (_parameter == null) {
                            _parameter = (String)obj;
                            setup();
                        }
                        return QueryLOV.this;
                    }
                };
        }
        return _param;
    }

    private void addAllSearchDefList(List<SavedSearchDef> savedSearchDefList) {
        getSavedSearchDefList().addAll(savedSearchDefList);
        for (SavedSearchDef ssd : savedSearchDefList) {
            ssd.setQuery(this);
        }
    }

    public void setSystemSavedSearchDefList(List<SavedSearchDef> systemSaveSearchDefList) {
        this._systemSavedSearchDefList = systemSaveSearchDefList;
    }

    public List<SavedSearchDef> getSystemSavedSearchDefList() {
        if (_systemSavedSearchDefList == null) {
            _systemSavedSearchDefList = new ArrayList<SavedSearchDef>();
        }
        return _systemSavedSearchDefList;
    }

    public void setQueryId(String queryId) {
        this._queryId = queryId;
    }

    public String getQueryId() {
        if (_queryId == null) {
            //if queryId is not specified in the managed bean
            //return the generic _entityName
            return _entityName;
        }
        return _queryId;
    }

    public void setQueryPersistenceHelper(AbstractQueryPersistenceHelper queryPersistenceHelper) {
        this._queryPersistenceHelper = queryPersistenceHelper;
    }

    private List<AttributeDef> getAttributeList() {
        if (_attributeList == null) {
            _attributeList = new ArrayList<AttributeDef>();
        }
        return _attributeList;
    }

    private Map<String, Object> getRecentSelections() {
        if (_recentSelections == null) {
            _recentSelections = new HashMap<String, Object>();
        }
        return _recentSelections;
    }

    public DummyMap getConvert() {
        if (_convert == null) {
            _convert = new DummyMap() {
                    public Object get(Object obj) {
                        if (obj == null) {
                            throw new IllegalArgumentException("Converter attribute not provided.");
                        }
                        //added codes to support dynamic conversion of sub-elements in the case of OrgNaturalAcct
                        //where you need to get only the NaturalAcct object.
                        _converterKey = obj.toString();
                        return getDynamicConverter();
                    }
                };
        }
        return _convert;
    }

    public void setShowAllRecordsByDefault(boolean showAllRecordsByDefault) {
        this._showAllRecordsByDefaultForNonAutoExecuteQD =
                showAllRecordsByDefault;
    }

    public boolean isShowAllRecordsByDefault() {
        return _showAllRecordsByDefaultForNonAutoExecuteQD;
    }

    public void setSupplementaryMap(Map<String, Object> supplementaryMap) {
        this._supplementaryMap = supplementaryMap;
    }

    public Map<String, Object> getSupplementaryMap() {
        return _supplementaryMap;
    }

    private Object getSupplementedModel(String attrName) {
        if (_supplementaryMap != null) {
            return _supplementaryMap.get(attrName);
        }
        return null;
    }

    public void setSubProperty(String subProperty) {
        this._subProperty = subProperty;
    }

    public String getSubProperty() {
        return _subProperty;
    }

    public void launchPopupListener(LaunchPopupEvent event) {
        event.setLaunchPopup(false);
        RichInputListOfValues lov =
            (RichInputListOfValues)event.getComponent();
        lov.setValue(event.getSubmittedValue());
    }

    public Converter getDynamicConverter() {
        if (_dynamicConverter == null) {
            if (_converterKey == null) {
                throw new IllegalStateException("Converter key not set. Please specify an attribute to be converted thru a dummy map like or thru the managed property.");
            }
            if (!hasInitialized) {
                setup();
            }
            _dynamicConverter = new DynamicConverter();
        }
        return _dynamicConverter;
    }

    public AttributeDef getKeyAttrDef() {
        if (_keyAttrDef == null) {
            String attribute = null;
            if (_subProperty != null) {
                attribute = (_subProperty + "." + _converterKey);
            } else {
                attribute = _converterKey;
            }
            _keyAttrDef = getAttributes().get(attribute);
            if (_keyAttrDef == null) {
                throw new IllegalArgumentException("Specified attribute do not exist: " +
                                                   attribute);
            }
        }
        return _keyAttrDef;
    }

    public void setParameter(String _parameter) {
        this._parameter = _parameter;
    }

    public String getParameter() {
        return _parameter;
    }

    public void setConverterKey(String _converterKey) {
        this._converterKey = _converterKey;
    }

    public String getConverterKey() {
        return _converterKey;
    }

    public List<Observer> getObservers() {
        if (_observers == null){
            _observers = new ArrayList<Observer>();
        }
        return _observers;
    }

    public void setObservers(List<Observer> list) {
        this._observers = list;
    }

    public void addObserver(Observer e) {
        getObservers().add(e);
    }

    public void removeObserver(Observer e) {
        getObservers().remove(e);
    }

    public void notifyObservers() {
        _logger.fine("QueryLOV.notifyObservers begin");
        for(Observer e: getObservers()){
            e.update(this, null);
        }
    }


    public class QueryDescriptorImpl extends FilterableQueryDescriptor{

        private SavedSearchDef _savedSearchDef; //retain copy for use during reset
        private Map<String, Object> _filterCriteria;
        private AttributeCriterion _currentCriterion;
        private Map<String, Object> _uiHintsMap;
        private ConjunctionCriterion _conjunctionCriterion;
        //List for hidden criterion so that they can be added back unchanged.
        private List<Criterion> _hiddenCriterions;

        public QueryDescriptorImpl(SavedSearchDef savedSearchDef) {
            _savedSearchDef = savedSearchDef;
            initialize();
        }
        
//        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
//        {
//            in.defaultReadObject();
//            initialize();
//        }
        public SavedSearchDef generateSavedSearch() {
            SavedSearchDef ssd = new SavedSearchDef();
            ssd.setAutoExecute(unbox((Boolean)getUIHints().get(UIHINT_AUTO_EXECUTE)));
            ssd.setDefaultSearch(unbox((Boolean)getUIHints().get(UIHINT_DEFAULT)));
            ssd.setDefaultConjunction(_conjunctionCriterion.getConjunction());
            ssd.setName(_savedSearchDef.getName() + "_copy");
            ssd.setMode((QueryDescriptor.QueryMode)getUIHints().get(UIHINT_MODE));
            ssd.setQuery(QueryLOV.this);
            ssd.setReadOnly(unbox((Boolean)getUIHints().get(UIHINT_IMMUTABLE)));
            ssd.setResultsId((String)getUIHints().get(UIHINT_RESULTS_COMPONENT_ID));
            ssd.setSaveResultsLayout(unbox((Boolean)getUIHints().get(UIHINT_SAVE_RESULTS_LAYOUT)));
            //            ssd.setShowInList(unbox((Boolean) getUIHints().get(UIHINT_SHOW_IN_LIST)));
            List<SearchFieldDef> searchFields =
                new ArrayList<SearchFieldDef>();
            for (Criterion criterion :
                 _conjunctionCriterion.getCriterionList()) {
                AttributeCriterionImpl attrCriterion =
                    (AttributeCriterionImpl)criterion;
                SearchFieldDef sfd = new SearchFieldDef();
                sfd.setAttrName(attrCriterion.getAttribute().getName());
                sfd.setOperator((OperatorDef)attrCriterion.getOperator().getValue());
                sfd.setQuery(QueryLOV.this);
                sfd.setRemovable(attrCriterion.isRemovable());
                //copy list to support proper reset;
                List copy = copy(attrCriterion.getValues());
                sfd.setValues(copy);
                searchFields.add(sfd);
            }
            ssd.setSearchFields(searchFields);
            return ssd;
        }

        public void initialize() {
            SavedSearchDef savedSearchDef =
                _savedSearchDef.copy(); //work with a copy
            List<Criterion> criterionList = new ArrayList<Criterion>();
            for (SearchFieldDef searchFieldDef :
                 savedSearchDef.getSearchFields()) {
                //shallow copy list to support reset
                List values = copy(searchFieldDef.getValues());
                //ensure values are not null to avoid el property exception isReadOnly blah blah...
                if (values.isEmpty()) {
                    values.add(BLANK);
                    values.add(BLANK);
                }
                Criterion criterion =
                    new AttributeCriterionImpl(searchFieldDef.getAttributeDef(),
                                               searchFieldDef.getOperator(),
                                               values,
                                               searchFieldDef.isRemovable());
                criterionList.add(criterion);
            }
            _currentCriterion = null;
            _conjunctionCriterion =
                    new ConjunctionCriterionImpl(savedSearchDef.getDefaultConjunction(),
                                                 criterionList);
            _hiddenCriterions = new ArrayList<Criterion>();
            _uiHintsMap = new HashMap<String, Object>();
            _uiHintsMap.put(UIHINT_AUTO_EXECUTE,
                            savedSearchDef.isAutoExecute());
            _uiHintsMap.put(UIHINT_MODE, savedSearchDef.getMode());
            _uiHintsMap.put(UIHINT_NAME, savedSearchDef.getName());
            _uiHintsMap.put(UIHINT_SAVE_RESULTS_LAYOUT,
                            savedSearchDef.isSaveResultsLayout());
            _uiHintsMap.put(UIHINT_SHOW_IN_LIST,
                            savedSearchDef.isShowInList());
            _uiHintsMap.put(UIHINT_IMMUTABLE, savedSearchDef.isReadOnly());
            _uiHintsMap.put(UIHINT_RESULTS_COMPONENT_ID,
                            savedSearchDef.getResultsId());
            _uiHintsMap.put(UIHINT_DEFAULT, savedSearchDef.isDefaultSearch());
        }

        private List copy(List list) {
            //shallow copy
            List copy = new ArrayList();
            copy.addAll(list);
            return copy;
        }

        public void addCriterion(String name) {
            // criterion
            SearchFieldDef sfd = new SearchFieldDef();
            sfd.setAttrName(name);
            //initialize dummy values
            sfd.getValues().add(BLANK);
            sfd.getValues().add(BLANK);
            sfd.setQuery(QueryLOV.this);
            Criterion criterion =
                new AttributeCriterionImpl(sfd.getAttributeDef(),
                                           sfd.getOperator(), sfd.getValues(),
                                           sfd.isRemovable());
            ((ConjunctionCriterionImpl)_conjunctionCriterion).addCriterion(criterion);

        }


        public void changeMode(QueryDescriptor.QueryMode mode) {
            _uiHintsMap.put(UIHINT_MODE, mode);
        }

        public SavedSearchDef getSavedSearchDef() {
            return _savedSearchDef;
        }

        public ConjunctionCriterion getConjunctionCriterion() {
            return _conjunctionCriterion;
        }

        public void setConjunctionCriterion(ConjunctionCriterion criterion) {
            _conjunctionCriterion = criterion;
        }

        public String getName() {
            return (String)getUIHints().get(UIHINT_NAME);
        }

        public void removeCriterion(Criterion criterion) {
            ((ConjunctionCriterionImpl)_conjunctionCriterion).removeCriterion(criterion);
        }

        public Map<String, Object> getUIHints() {
            return _uiHintsMap;
        }

        public AttributeCriterion getCurrentCriterion() {
            return _currentCriterion;
        }

        public void setCurrentCriterion(AttributeCriterion attrCriterion) {
            this._currentCriterion = attrCriterion;
        }

        @Override
        public Map<String, Object> getFilterCriteria() {
            return _filterCriteria;
        }

        @Override
        public void setFilterCriteria(Map<String, Object> filterCriteria) {
            _filterCriteria = filterCriteria;
        }

        public void addHiddenCriterion(AttributeCriterionImpl criterion) {
            _hiddenCriterions.add(criterion);
        }

        public void removeHiddenCriterion(AttributeCriterionImpl criterion) {
            _hiddenCriterions.remove(criterion);
        }

        private String formatDateForJPQL(Date date) {
            SimpleDateFormat formatter = new SimpleDateFormat();
            formatter.applyPattern("yyyy-MM-dd");
            StringBuilder builder = new StringBuilder();
            builder.append("{d '");
            builder.append(formatter.format(date));
            builder.append("'}");
            return builder.toString();
        }


        private Object safeGetValue(List list, int index) {
            if (list == null) {
                return null;
            }
            if (list.size() <= index) { //index is zero based
                return null;
            }
            return list.get(index);
        }

        private String format(Object value, String type) {
            if (AttributeDef.isDateType(type)) {
                if(value == null){
                    value = new Date();
                }
                return formatDateForJPQL((Date)value);
            } else if(value != null){
                return value.toString();
            } else{
                return "";
            }
        }


        private Date getDateFrom(Object value) {
            if (value != null && value instanceof Date) {
                return (Date)value;
            }
            return BLANK_STARTING_DATE;
        }

        private Date getDateTo(Object value) {
            if (value != null && value instanceof Date) {
                return (Date)value;
            }
            return BLANK_ENDING_DATE;
        }


        public String toString() {
            //Before appending text to the builder ensure that you put space first
            StringBuilder builder = new StringBuilder();
            boolean ignoreConjunction = true;
            for (Criterion criterion :
                 _conjunctionCriterion.getCriterionList()) {
                if (!(criterion instanceof AttributeCriterion)) {
                    continue;
                }
                AttributeCriterion attrCriterion =
                    (AttributeCriterion)criterion;
                AttributeDescriptor.Operator operator =
                    attrCriterion.getOperator();
                if (operator.getValue().equals(OperatorDef.NO_OPERATOR)) {
                    //exclude criterions with no operator
                    continue;
                }
                String type =
                    attrCriterion.getAttribute().getType().getCanonicalName();
                ColumnDescriptorImpl descriptor =
                    (ColumnDescriptorImpl)attrCriterion.getAttribute();
                String attrName = descriptor.getName();
                //no before conjuction for the first criteria
                if (!ignoreConjunction) {
                    ConjunctionCriterion.Conjunction conjunction =
                        _conjunctionCriterion.getConjunction();
                    builder.append(SPACE);
                    builder.append(conjunction);
                }
                ignoreConjunction = false;
                builder.append(SPACE);
                //enclose each critetia in a bracket (criteria_1) AND (criteria_2)
                builder.append(OPEN_BRACKET);
                //build attribute string-- [o.job.jobTitle]
                builder.append(buildAttributeExpression(attrName, type));

                List values = attrCriterion.getValues();

                //space before operator statements
                builder.append(SPACE);
                Object value = safeGetValue(values, 0);
                OperatorDef oper = (OperatorDef)operator.getValue();
                int count = 0;
                switch (oper) {

                case EQUALS:
                    if (value == null || "".equals(value)) {
                        builder.append(_IS_NULL);
                        break;
                    }
                    if (AttributeDef.isDateType(type)) { //disregard time element
                        Date date = (Date)value;
                        Date dateFrom = MyUtils.removeTimeElement(date);
                        Date dateTo = MyUtils.addOneDay(dateFrom);
                        builder.append(" >= ");
                        builder.append(formatDateForJPQL(dateFrom));
                        builder.append(_AND);
                        //another attribute string
                        builder.append(buildAttributeExpression(attrName,
                                                                type));
                        builder.append(" < ");
                        builder.append(formatDateForJPQL(dateTo));
                        break;
                    }

                    if (AttributeDef.isNumericType(type)) {
                        //blank values on numeric types is assumed zero
                        if (value == null || "".equals(value)) {
                            value = 0;
                        }
                        builder.append(" = ");
                        builder.append(value);
                        break;
                    }
                    //default
                    builder.append(" = ");
                    builder.append(formatStringForJPQL(value.toString()));
                    break;
                case NOT_EQUALS:
                    if (value == null || "".equals(value)) {
                        builder.append(_IS_NOT_NULL);
                        break;
                    }
                    if (AttributeDef.isDateType(type)) { //disregard time element
                        Date date = (Date)value;
                        Date dateFrom = MyUtils.removeTimeElement(date);
                        Date dateTo = MyUtils.addOneDay(dateFrom);
                        builder.append(" < ");
                        builder.append(formatDateForJPQL(dateFrom));
                        builder.append(_OR);
                        //another attribute string
                        builder.append(buildAttributeExpression(attrName,
                                                                type));
                        builder.append(" >= ");
                        builder.append(formatDateForJPQL(dateTo));
                        break;
                    }

                    if (AttributeDef.isNumericType(type)) {
                        builder.append(" <> ");
                        builder.append(value);
                        break;
                    }
                    //default
                    builder.append(" <> ");
                    builder.append(formatStringForJPQL(value.toString()));
                    break;
                case GREATER_THAN:
                    builder.append(" > ");
                    builder.append(format(value, type));
                    break;
                case GREATER_THAN_EQUALS:
                    builder.append(" >= ");
                    builder.append(format(value, type));
                    break;
                case LESS_THAN:
                    builder.append(" < ");
                    builder.append(format(value, type));
                    break;
                case LESS_THAN_EQUALS:
                    builder.append(" <= ");
                    builder.append(format(value, type));
                    break;

                case STARTS_WITH:
                    builder.append("LIKE '");
                    builder.append(getValueString(value));
                    builder.append("%'");
                    break;
                case ENDS_WITH:
                    builder.append("LIKE '%");
                    builder.append(getValueString(value));
                    builder.append("'");
                    break;
                case CONTAINS:
                    builder.append("LIKE '%");
                    builder.append(getValueString(value));
                    builder.append("%'");
                    break;
                case DOES_NOT_CONTAIN:
                    builder.append("NOT LIKE '%");
                    builder.append(getValueString(value));
                    builder.append("%'");
                    break;
                case LIKE:
                    builder.append("LIKE '");
                    builder.append(getValueString(value));
                    builder.append("'");
                    break;
                case BETWEEN:
                    if (type.equals(DATE_TYPE) ||
                        type.equals(TIMESTAMP_TYPE)) {
                        Date dateFrom =
                            MyUtils.removeTimeElement((getDateFrom(value)));
                        Date dateTo = getDateTo(safeGetValue(values, 1));
                        //exclude time elements in search
                        dateTo = MyUtils.addOneDay(dateTo);
                        dateTo = MyUtils.removeTimeElement(dateTo);
                        builder.append(">= ");
                        builder.append(formatDateForJPQL(dateFrom));
                        builder.append(_AND);
                        //another attribute string
                        builder.append(buildAttributeExpression(attrName,
                                                                type));
                        builder.append(" < ");
                        builder.append(formatDateForJPQL(dateTo));
                        break;
                    }
                    if (AttributeDef.isNumericType(type)) {
                        //empty string for numeric types assumed to be zero
                        if (value == null || "".equals(value)) {
                            value = 0;
                        }
                        builder.append("BETWEEN ");
                        builder.append(value);
                        builder.append(_AND);
                        builder.append(SPACE);
                        builder.append(safeGetValue(values, 1));
                        break;
                    }
                    break;
                case IN:
                    //... IN ( 'x', 'y', 'z')
                    builder.append("IN ( ");
                    if (value instanceof List) {
                        for (Object obj : (List)value) {
                            if (count > 0) {
                                builder.append(" , ");
                            }
                            if (AttributeDef.isNumericType(type)) {
                                builder.append(obj);
                            } else {
                                builder.append(formatStringForJPQL((String)obj));
                            }
                            count++;
                        }
                    } else {
                        if (AttributeDef.isNumericType(type)) {
                            builder.append(value);
                        } else {
                            builder.append(formatStringForJPQL((String)value));
                        }
                    }
                    builder.append(CLOSE_BRACKET);
                    break;
                case NOT_IN:
                    //... NOT IN ( 'x', 'y', 'z')
                    builder.append("NOT IN ( ");
                    if (value instanceof List) {
                        for (Object obj : (List)value) {
                            if (count > 0) {
                                builder.append(" , ");
                            }
                            if (AttributeDef.isNumericType(type)) {
                                builder.append(obj);
                            } else {
                                builder.append(formatStringForJPQL((String)obj));
                            }
                            count++;
                        }
                    } else {
                        if (AttributeDef.isNumericType(type)) {
                            builder.append(value);
                        } else {
                            builder.append(formatStringForJPQL((String)value));
                        }
                    }
                    builder.append(CLOSE_BRACKET);
                    //include NULL values as well
                    //... OR <attr> IS NULL
                    builder.append(_OR);
                    builder.append(buildAttributeExpression(attrName, type));
                    builder.append(_IS_NULL);
                    break;
                default:
                    //should not come to this point;
                    System.out.println("----------Check problematic Query.QueryDescriptorImpl.toString()-----------");
                } //end switch
                //every criterion should be enclosed in brackets
                builder.append(CLOSE_BRACKET);
            } //end for
            return builder.toString();
        }

    }


    // A QueryModel is the model holding all available saved searches -- represented by QueryDescriptor.

    public class QueryModelImpl extends QueryModel {
        private List<AttributeDescriptor> _attrDescriptors;

        public QueryModelImpl() {
        }

        public List<AttributeDescriptor> getAttributes() {
            if (_attrDescriptors == null) {
                if (_attributes != null) {
                    _attrDescriptors = new ArrayList<AttributeDescriptor>();
                    for (AttributeDef attr : _attributes.values()) {
                        if (attr.isQueriable()) {
                            AttributeDescriptor attrDesc =
                                new ColumnDescriptorImpl(attr);
                            _attrDescriptors.add(attrDesc);
                        }
                    }
                    return _attrDescriptors;
                } else {
                    return Collections.emptyList();
                }
            }
            return _attrDescriptors;
        }

        //Create a QueryDescriptor.

        public QueryDescriptor create(String name, QueryDescriptor qdBase) {
            // If the queryDescriptor already exists, then just return that
            if (getQueries().get(name) != null) {
                return getQueries().get(name);
            }
            // We need a create a new QueryDescriptor based on the existing one
            if (qdBase != null) {
                QueryDescriptorImpl qdImpl = (QueryDescriptorImpl)qdBase;
                SavedSearchDef savedSearchDef = qdImpl.generateSavedSearch();
                savedSearchDef.setName(name);
                savedSearchDef.setQueryId(getQueryId());
                //edited by owie
                savedSearchDef.setReadOnly(false);
                System.out.println("owie's edit");
                //*******
                QueryDescriptorImpl newQueryDesc =
                    new QueryDescriptorImpl(savedSearchDef);

                //Mark this newly created QD as a user saved search
                newQueryDesc.getUIHints().put(QueryDescriptor.UIHINT_IMMUTABLE,
                                              false);
                getQueries().put(name, newQueryDesc);
                if (getQueryPersistenceHelper() != null) {
                    getQueryPersistenceHelper().mergeSavedSearchDef(savedSearchDef);
                }
                //Reset the base queryDescriptor
                reset(qdBase);
                return newQueryDesc;

            }
            return null;


        }

        public void delete(QueryDescriptor queryDescriptor) {
            // we can't delete systemQueries
            boolean immutable =
                unbox((Boolean)queryDescriptor.getUIHints().get(QueryDescriptor.UIHINT_IMMUTABLE));
            if (!immutable) {
                // Remove the QueryDescriptor from the QueryModel at this point.
                getQueries().remove(queryDescriptor.getName());
                //persistence
                if (getQueryPersistenceHelper() != null) {
                    SavedSearchDef ssd =
                        ((QueryDescriptorImpl)queryDescriptor).getSavedSearchDef();
                    getQueryPersistenceHelper().removeSavedSearchDef(ssd);
                }
            }
        }

        public void update(QueryDescriptor queryDescriptor,
                           Map<String, Object> uiHints) {
            // Update the UIHints. If the name changes the update the _qdSet
            if (uiHints != null && !uiHints.isEmpty()) {
                // If the current name of the QD changes, delete the old entry from the _qdSet and add a
                // new entry.
                String newName =
                    (String)uiHints.get(QueryDescriptor.UIHINT_NAME);
                String currentName = queryDescriptor.getName();
                queryDescriptor.getUIHints().putAll(uiHints);
                SavedSearchDef ssd =
                    ((QueryDescriptorImpl)queryDescriptor).getSavedSearchDef();
                ssd.update(uiHints);
                if (!currentName.equals(newName)) {
                    getQueries().remove(currentName);
                    getQueries().put(newName, queryDescriptor);
                }
                //persistence
                if (getQueryPersistenceHelper() != null) {
                    getQueryPersistenceHelper().mergeSavedSearchDef(ssd);
                }
            }
        }

        /**
         * Resets the QueryDescriptor back to its original state. Removes all added criterion objects
         * and resets the values of all the fields to its default. Also resets the conjunction to its
         * default
         *
         * @param queryDescriptor
         */
        public void reset(QueryDescriptor queryDescriptor) {
            ((QueryDescriptorImpl)queryDescriptor).initialize();
            QueryLOV.this.prepare();
        }


        // "systemQueries" attribute EL reachable

        public List<QueryDescriptor> getSystemQueries() {
            List<QueryDescriptor> systemQueries =
                new ArrayList<QueryDescriptor>();
            for (Map.Entry entry : getQueries().entrySet()) {
                QueryDescriptor qd = (QueryDescriptor)entry.getValue();
                boolean immutable =
                    unbox((Boolean)qd.getUIHints().get(QueryDescriptor.UIHINT_IMMUTABLE));
                if (immutable) {
                    systemQueries.add(qd);
                }
            }
            return systemQueries;
        }

        // "userQueries" attribute EL reachable

        public List<QueryDescriptor> getUserQueries() {
            List<QueryDescriptor> userQueries =
                new ArrayList<QueryDescriptor>();
            for (Map.Entry entry : getQueries().entrySet()) {
                QueryDescriptor qd = (QueryDescriptor)entry.getValue();
                boolean immutable =
                    unbox((Boolean)qd.getUIHints().get(QueryDescriptor.UIHINT_IMMUTABLE));
                if (!immutable) {
                    userQueries.add(qd);
                }
            }
            return userQueries;
        }

        public void setCurrentDescriptor(QueryDescriptor qd) {
            if (qd != null)
                _currentDescriptor = qd;
        }


    }

    public class ConjunctionCriterionImpl extends ConjunctionCriterion {
        private List<Criterion> _criterionList;
        private ConjunctionCriterion.Conjunction _conjunction;

        public ConjunctionCriterionImpl(Conjunction conjunction,
                                        List<Criterion> criterionList) {
            _conjunction = conjunction;
            _criterionList = criterionList;
        }

        public Object getKey(Criterion criterion) {
            int index = _criterionList.indexOf(criterion);
            if (index != -1) {
                return Integer.toString(index);
            }
            return null;
        }

        public Criterion getCriterion(Object key) {
            int index = -1;
            if (key != null && key instanceof Integer) {
                index = (Integer)key;
            } else if (key instanceof String) {
                index = Integer.parseInt((String)key);
            }
            List<Criterion> criterionList = getCriterionList();
            if (index > -1 && index < criterionList.size())
                return criterionList.get(index);

            return null;
        }

        public ConjunctionCriterion.Conjunction getConjunction() {
            return this._conjunction;
        }

        public List<Criterion> getCriterionList() {
            return _criterionList;
        }

        public void setConjunction(ConjunctionCriterion.Conjunction conjunction) {
            this._conjunction = conjunction;
        }

        public void removeCriterion(Criterion criterion) {
            _criterionList.remove(criterion);
        }

        public void addCriterion(Criterion criterion) {
            _criterionList.add(criterion);
        }

    }

    public class AttributeCriterionImpl extends AttributeCriterion {
        private AttributeDef _attrDef;
        private AttributeDescriptor.Operator _operator;
        private List _values;
        private boolean _removable;
        private AttributeDescriptor _attrDescriptor;


        public AttributeCriterionImpl(AttributeDef attributeDef,
                                      OperatorDef operatorDef, List values,
                                      boolean removable) {
            this._attrDef = attributeDef;
            this._values = values;
            this._removable = removable;
            this._attrDescriptor = new ColumnDescriptorImpl(attributeDef);
            this._operator =
                    ((ColumnDescriptorImpl)_attrDescriptor).getOperator(operatorDef);
        }

        public AttributeDescriptor getAttribute() {
            return _attrDescriptor;
        }

        public AttributeDescriptor.Operator getOperator() {
            return _operator;
        }


        /**
         * The list of operators returned for an AttributeCriterion could be different from those
         * returned for an AttributeDescriptor. For e.g., for an LOV attribute that supports multiple
         * selection it makes more sense to support "In" and "Not In" operators rather than "Equals /
         * Not Equals".
         * @return
         */
        public Map<String, AttributeDescriptor.Operator> getOperators() {

            Map<String, AttributeDescriptor.Operator> optrMap =
                new LinkedHashMap<String, AttributeDescriptor.Operator>();
            if (((ColumnDescriptorImpl)getAttribute()).isLOV()) {
                //inputListOfValues cannot support mutiple selection
                if (!getAttribute().getComponentType().equals(AttributeDescriptor.ComponentType.inputListOfValues)) {
                    AttributeDescriptor.Operator in =
                        ((ColumnDescriptorImpl)_attrDescriptor).getOperator(OperatorDef.IN);
                    optrMap.put(in.getLabel(), in);
                    AttributeDescriptor.Operator notIn =
                        ((ColumnDescriptorImpl)_attrDescriptor).getOperator(OperatorDef.NOT_IN);
                    optrMap.put(notIn.getLabel(), notIn);
                }
            }
            //plus the default supported operators
            List<OperatorDef> operatorList = _attrDef.getSupportedOperators();
            for (OperatorDef operator : operatorList) {
                AttributeDescriptor.Operator optr =
                    ((ColumnDescriptorImpl)_attrDescriptor).getOperator(operator);
                optrMap.put(optr.getLabel(), optr);
            }
            return optrMap;
        }

        public List getValues() {
            if (_values != null) {
                return _values;
            }
            return Collections.emptyList();
        }

        public void setValues(List values) {
            this._values = values;
        }

        public boolean isRemovable() {
            return this._removable;
        }

        public void setOperator(AttributeDescriptor.Operator operator) {
            this._operator = operator;
        }

        /**
         * Returns the ComponentType specific to an AttributeCriterion. This may be different from the
         * ComponentType of the AttributeDescriptor, based on the operator chosen or whether
         * multi-select is enabled or not. For e.g., for LOV attributes, <br/>
         * - when multi-select is enabled, <br/>
         *   - for operator 'In / Not In', the component type will always be selectManyChoice regardless
         *     of the default type or dataType. <br/>
         *   - For all other operators it's an inputText (inputDate or inputNumberSpinbox for Date and
         *     Number datatypes respectively.)
         * - when multi-select is disabled, <br/>
         *   - for operator Equals/Not Equals, it will be the default type (of the attribute)<br/>
         *   - for all other operators, an inputText (or inputDate or inputNumberSpinbox)
         *
         * @param operator the operator for which the component type needs to be determined
         * @return a ComponentType
         */
        @Override
        public AttributeDescriptor.ComponentType getComponentType(AttributeDescriptor.Operator operator) {
            ColumnDescriptorImpl attrDesc =
                (ColumnDescriptorImpl)getAttribute();
            boolean isMultiSelectOper = attrDesc.hasVariableOperands(operator);
            if (attrDesc.isLOV()) {
                Object model = getAttribute().getModel();
                if (isMultiSelectOper) {
                    // always return selectManyChoice for multiSelect enabled operators
                    return (model instanceof ListOfValuesModel) ?
                           AttributeDescriptor.ComponentType.inputComboboxListOfValues :
                           AttributeDescriptor.ComponentType.selectManyChoice;
                }
                if (operator.getValue().equals(OperatorDef.EQUALS) ||
                    operator.getValue().equals(OperatorDef.NOT_EQUALS)) {
                    return (model instanceof ListOfValuesModel) ?
                           AttributeDescriptor.ComponentType.inputListOfValues :
                           AttributeDescriptor.ComponentType.selectOneChoice;
                }
            }
            // the base component type is used (based on the datatype)
            String typeName = attrDesc.getType().getName();
            if (AttributeDef.isNumericType(typeName)) {
                return AttributeDescriptor.ComponentType.inputNumberSpinbox;
            }
            if (typeName.equals(DATE_TYPE) ||
                typeName.equals(TIMESTAMP_TYPE)) {
                return AttributeDescriptor.ComponentType.inputDate;
            }
            return AttributeDescriptor.ComponentType.inputText;
        }
    }

    public final class ColumnDescriptorImpl extends ColumnDescriptor {
        private AttributeDef _attrDef;

        public ColumnDescriptorImpl(AttributeDef attrDef) {
            _attrDef = attrDef;
        }


        public String getDescription() {
            //            return _attrDef.getDescription(); not yet localized
            return null;
        }

        public Converter getConverter() {
            Application application =
                FacesContext.getCurrentInstance().getApplication();
            Class type = getType();
            if (Number.class.isAssignableFrom(type)) {
                return application.createConverter(Number.class);
            }
            if (Date.class.isAssignableFrom(type)) {
                return application.createConverter(Date.class);
            }
            return null;
        }

        public String getFormat() {
            return _attrDef.getFormat();
        }

        public String getLabel() {
            String attrLabel = _attrDef.getLabel();
            String accessorName = _attrDef.getAccessorName();
            if (_attrDef.getPropertiesBundle() != null) {
                String resourceLabel =
                    JSFUtils.getStringFromBundle(_attrDef.getPropertiesBundle(),
                                                 attrLabel, attrLabel);

                return (accessorName != null) ?
                       AttributeDef.concatWithDot(accessorName,
                                                  resourceLabel) :
                       resourceLabel;
            }
            return (accessorName != null) ?
                   AttributeDef.concatWithDot(accessorName, attrLabel) :
                   attrLabel;
        }

        /**
         * Returns true if criterion is indexed and not required.
         *
         * @return true if criterion is indexed and not required else false.
         */
        public boolean isIndexed() {
            //only when criterion is indexed but not required it will be considered as indexed
            return (_attrDef.isIndexed() && !_attrDef.isMandatory());
        }

        public boolean isLOV() {
            AttributeDescriptor.ComponentType componentType =
                getComponentType();
            return componentType.equals(AttributeDescriptor.ComponentType.inputListOfValues) ||
                componentType.equals(AttributeDescriptor.ComponentType.inputComboboxListOfValues) ||
                componentType.equals(AttributeDescriptor.ComponentType.selectManyChoice) ||
                componentType.equals(AttributeDescriptor.ComponentType.selectOneChoice) ||
                componentType.equals(AttributeDescriptor.ComponentType.selectOneListbox);
        }


        /**
         * Based on the component type of the attribute, it returns an appropriate model object expected
         * of that component
         * @return
         */
        public Object getModel() {
            Object model = getSupplementedModel(_attrDef.getName());
            if (model != null) {
                return model;
            }
            AttributeDescriptor.ComponentType compType = getComponentType();
            return getModel(compType);
        }

        public Object getModel(AttributeDescriptor.ComponentType compType) {
            if (compType.equals(AttributeDescriptor.ComponentType.selectOneChoice) ||
                compType.equals(AttributeDescriptor.ComponentType.selectManyChoice)) {
                List selectItems = new ArrayList();
                //use custom properties as select items
                for (Map.Entry<String, String> entry :
                     _attrDef.getCustomProperties().entrySet()) {
                    selectItems.add(new SelectItem(entry.getValue(),
                                                   entry.getKey()));
                }
                return selectItems;
            }
            return null;
        }

        public String getName() {
            return _attrDef.getName();
        }


        public Set<AttributeDescriptor.Operator> getSupportedOperators() {
            List<OperatorDef> operatorList = _attrDef.getSupportedOperators();
            Set<AttributeDescriptor.Operator> optrSet =
                new HashSet<AttributeDescriptor.Operator>();
            for (OperatorDef operator : operatorList) {
                AttributeDescriptor.Operator optr = new OperatorImpl(operator);
                optrSet.add(optr);
            }
            return optrSet;
        }

        public Class getType() {
            return _attrDef.getType();
        }

        public AttributeDescriptor.ComponentType getComponentType() {
            Object model = getSupplementedModel(_attrDef.getName());
            if (model != null) {
                if (model instanceof ListOfValuesModel) {
                    //inputComboboxListOfValues should return at least an empty list.
                    return ((ListOfValuesModel)model).getItems() == null ?
                           AttributeDescriptor.ComponentType.inputListOfValues :
                           AttributeDescriptor.ComponentType.inputComboboxListOfValues;
                }
                if (model instanceof List) {
                    return AttributeDescriptor.ComponentType.selectManyChoice;
                }
                //undefined model continue with the usual below;
            }
            AttributeDescriptor.ComponentType componentType =
                COMPONENT_MAP.get(_attrDef.getComponentType());
            return (componentType != null) ? componentType :
                   ComponentType.inputText;
        }

        public boolean isReadOnly() {
            return false;
        }

        /**
         * Returns true if this criterion is a required field or is the only indexed field in the
         * criterion list.
         *
         * @return true if single indexed criterion exist else false
         */
        public boolean isRequired() {
            //building of the JPQL String could throw exception if appropriate controls are not marks as required.
            //handle with care :)
            String type = _attrDef.getType().getName();
            return false;//_attrDef.isMandatory() ||
//                AttributeDef.isNumericType(type) ||
//                AttributeDef.isDateType(type) || isLOV();
        }

        public AttributeDescriptor.Operator getOperator(OperatorDef operator) {
            return new OperatorImpl(operator);
        }

        public int getLength() {
            return _attrDef.getLength();
        }

        public int getMaximumLength() {
            return _attrDef.getMaximumLength();
        }

        /**
         * Determines if the passed in operator can be used with multiple values, like the IN, Not IN
         * @param operator
         * @return boolean
         */
        public boolean hasVariableOperands(AttributeDescriptor.Operator operator) {
            if (operator == null)
                return false;
            OperatorImpl operImpl = (OperatorImpl)operator;
            return operImpl.getOperatorDef().hasVariableOperands();
        }

        /**
         * Determines if the operator requires showing the default component type. The default component
         * type of the attribute is used for operator Equals / Not Equals.
         * @param operator
         * @return
         */
        public boolean useDefaultComponentType(AttributeDescriptor.Operator operator) {
            OperatorImpl operImpl = (OperatorImpl)operator;
            return (operImpl.getOperatorDef().equals(OperatorDef.EQUALS) ||
                    operImpl.getOperatorDef().equals(OperatorDef.NOT_EQUALS) ||
                    operImpl.getOperatorDef().equals(OperatorDef.NO_OPERATOR));
        }

        public int getWidth() {
            return 0;
        }

        public String getAlign() {
            String type = _attrDef.getType().getName();
            if (type.equals(TIMESTAMP_TYPE) || type.equals(DATE_TYPE)) {
                return "center";
            }
            return "start";
        }

        public class OperatorImpl extends AttributeDescriptor.Operator {
            private OperatorDef _operator;

            public OperatorImpl(OperatorDef operator) {
                if (operator != null)
                    _operator = operator;
                else
                    _operator = OperatorDef.NO_OPERATOR;
            }

            public String getLabel() {
                return _operator.getLabel();
            }

            public Object getValue() {
                return _operator;
            }

            // Returns the default operandCount for the operator. Except when the operator is null it
            // returns 1 and if operandCount is -1, indicating unlimited operands, it returns 1, as from
            // the UI standpoint a selectmanyChoice will be rendered.

            public int getOperandCount() {
                if (_operator != null) {
                    int operandCount = _operator.getOperandCount();
                    if (operandCount != -1)
                        return operandCount;
                }
                return 1;
            }

            @Override
            public boolean equals(Object operator) {
                if (operator != null && operator instanceof OperatorImpl) {
                    return (this._operator.equals(((OperatorImpl)operator).getOperatorDef()));
                }
                return false;
            }

            @Override
            public String toString() {
                return _operator.getLabel();
            }

            private OperatorDef getOperatorDef() {
                return _operator;
            }
        }


    }

    public class ListOfValuesModelImpl extends ListOfValuesModel {
        private TableModel _tableModel;

        public ListOfValuesModelImpl() {
            _tableModel = new TableModelImpl();

        }

        /**
         * Not applicable as items are only supported in comboLOV
         * @return
         */
        @Override
        public List getItems() {
            return null;
        }

        @Override
        public QueryModel getQueryModel() {
            return QueryLOV.this.getQueryModel();
        }

        /**
         * Not applicable as items are only supported in comboLOV
         * @return
         */
        @Override
        public List getRecentItems() {
            return null;
        }

        @Override
        public TableModel getTableModel() {
            return _tableModel;
        }

        @Override
        public boolean isAutoCompleteEnabled() {
            return false;
        }

        public void performQuery(QueryDescriptor qd) {
            setSearchCriteria(qd.toString());
            prepare();
        }

        private Object _getRowData(Object selectedRowKey) {
            if (selectedRowKey != null &&
                selectedRowKey instanceof RowKeySet) {
                Iterator selection = ((RowKeySet)selectedRowKey).iterator();
                while (selection.hasNext()) {
                    Object rowKey = selection.next();
                    CollectionModel model =
                        getTableModel().getCollectionModel();
                    model.setRowKey(rowKey);
                    JUCtrlHierNodeBinding node =
                        (JUCtrlHierNodeBinding)model.getRowData();
                    DCDataRow row = (DCDataRow)node.getRow();
                    return row.getDataProvider();
                }
            }
            return null;
        }

        /**
         * @param value
         * @return
         */
        public List<Object> autoCompleteValue(Object value) {
            return Collections.emptyList();
        }

        public void valueSelected(Object value) {
            //reset the query to original citerions
            ((QueryDescriptorImpl)getCurrentDescriptor()).initialize();
        }


        public QueryDescriptor getQueryDescriptor() {
            return getCurrentDescriptor();
        }


        @Override
        public Object getValueFromSelection(Object object) {
            Object selected = _getRowData(object);

            if (_subProperty != null) {
                selected = MyUtils.getProperty(selected, _subProperty);
            }
            getRecentSelections().put(selected.toString(), selected);
            return selected;
        }

        public void setTableMOdel(TableModel tableModel) {
            this._tableModel = tableModel;
        }


    }

    public class TableModelImpl extends TableModel{
        private List<ColumnDescriptor> _columnDescriptors;

        public TableModelImpl() {
        }

        @Override
        public CollectionModel getCollectionModel() {
            return getInternalCollectionModel();
        }

        @Override
        public List<ColumnDescriptor> getColumnDescriptors() {
            if (_columnDescriptors == null) {
                if (_attributes != null) {
                    _columnDescriptors = new ArrayList<ColumnDescriptor>();
                    for (AttributeDef attr : _attributes.values()) {
                        ColumnDescriptor cd = new ColumnDescriptorImpl(attr);
                        _columnDescriptors.add(cd);
                    }
                    return _columnDescriptors;
                } else {
                    return Collections.emptyList();
                }
            }
            return _columnDescriptors;
        }

    }

    private class DynamicConverter implements Converter {
        public Object getAsObject(FacesContext facesContext,
                                  UIComponent uiComponent, String input) {

            if (input == null || BLANK.equals(input)) {
                return null;
            }
            //try to get from recent selections
            Object object = getRecentSelections().get(input);
            if (object != null) {
                return object;
            }
            //query from db
            //could include in the criteria the hidden criterions, but not yet supported
            object = executeQuery(input);
            if (object != null) {
                //put in the recent selections as well to avoid querying back to db
                if (_subProperty != null) {
                    object = MyUtils.getProperty(object, _subProperty);
                }
                getRecentSelections().put(input, object);
                return object;
            }
            FacesMessage message = new FacesMessage();
            message.setSummary("Invalid input");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            //            throw new ConverterException(message);//commented out because it raising
            // an undesirable exception behavior in adf faces which is being caused by the
            //auto launching of the popup -  pino

            EditableValueHolder component = (EditableValueHolder)uiComponent;
            component.setValid(false);
            facesContext.addMessage(uiComponent.getClientId(facesContext),
                                    message);
            return null;

        }

        private Object executeQuery(String input) {
            List result;
            AttributeDef attrDef = getKeyAttrDef();
            StringBuilder builder = new StringBuilder();
            builder.append(getSelectClause());
            builder.append(_WHERE_CLAUSE);
            builder.append(buildAttributeExpression(attrDef.getName(),
                                                    attrDef.getType().getName()));
            builder.append(" = ");
            if (AttributeDef.isNumericType(attrDef.getType().getName())) {
                builder.append(input);
            } else {
                builder.append(formatStringForJPQL(input));
            }
            BindingContainer bindings =
                BindingContext.getCurrent().getCurrentBindingsEntry();
            //reuse the same operator used by the tree binding

            OperationBinding oper = bindings.getOperationBinding(_operName);
            if(oper == null){
                throw new RuntimeException("Unable to find operation binding :" + _operName + " for LOV " + _parameter);
            }
            oper.getParamsMap().put("jpqlStmt", builder.toString());
            //retrieve only one
            oper.getParamsMap().put("maxResults", 1);
            result = (List)oper.execute();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        }

        public String getAsString(FacesContext facesContext,
                                  UIComponent uiComponent, Object object) {
            if (object == null)
                return "";
            if (object instanceof String) {
                return (String)object;
            }
            Object result = null;
            result = MyUtils.getProperty(object, getConverterKey());
            return result != null ? result.toString() : "";
        }
    }
}
