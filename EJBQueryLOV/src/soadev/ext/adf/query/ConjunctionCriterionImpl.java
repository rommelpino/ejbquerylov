package soadev.ext.adf.query;

import java.util.List;

import oracle.adf.view.rich.model.ConjunctionCriterion;
import oracle.adf.view.rich.model.Criterion;

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