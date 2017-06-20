package org.n52.cache;

import org.n52.io.request.RequestParameterSet;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;

import java.util.Optional;

public interface DataCache {
    boolean isDataCached(RequestParameterSet parameters);

    Optional<DataCollection<Data<AbstractValue< ? >>>> getData(RequestParameterSet parameters);
    
    void putDataForParameters(RequestParameterSet parameters, DataCollection<Data<AbstractValue< ? >>> data);

}
