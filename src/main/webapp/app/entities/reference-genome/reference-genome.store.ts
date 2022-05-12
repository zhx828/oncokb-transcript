import { IReferenceGenome } from 'app/shared/model/reference-genome.model';
import { IRootStore } from 'app/stores';
import axios from 'axios';
import CrudStore from 'app/shared/util/crud-store';

const apiUrl = 'api/reference-genomes';

export class ReferenceGenomeStore extends CrudStore<IReferenceGenome> {
  constructor(protected rootStore: IRootStore) {
    super(rootStore, apiUrl);
  }
}

export default ReferenceGenomeStore;
