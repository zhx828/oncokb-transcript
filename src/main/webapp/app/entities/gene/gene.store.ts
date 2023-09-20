import { IGene } from 'app/shared/model/gene.model';
import { IRootStore } from 'app/stores';
import axios from 'axios';
import PaginationCrudStore from 'app/shared/util/pagination-crud-store';
import { ICrudSearchAction } from 'app/shared/util/jhipster-types';
import { geneClient } from 'app/shared/api/clients';

const apiUrl = 'api/genes';
const apiSearchUrl = 'api/_search/genes';

export class GeneStore extends PaginationCrudStore<IGene> {
  searchEntities: ICrudSearchAction<IGene> = this.readHandler(this.getSearch);
  findAllGeneEntities = this.readHandler(this.findAllGene);

  constructor(protected rootStore: IRootStore) {
    super(rootStore, apiUrl);
  }
  *getSearch({ query, page, size, sort }) {
    const result = yield axios.get<IGene[]>(`${apiSearchUrl}?query=${query}${sort ? `&page=${page}&size=${size}&sort=${sort}` : ''}`);
    this.entities = result.data;
    this.totalItems = result.headers['x-total-count'];
    return result;
  }

  *findAllGene(hugoSymbol, page?, size?, sort?) {
    const query = hugoSymbol ? `?hugoSymbol.equals=${hugoSymbol}` : '';
    const result = yield axios.get<IGene[]>(`api/genes/${query}`);
    this.entities = result.data;
    this.totalItems = result.headers['x-total-count'];
    return result;
  }
}

export default GeneStore;
