import { IEnsemblGene } from 'app/shared/model/ensembl-gene.model';
import { IAlteration } from 'app/shared/model/alteration.model';
import { EnsemblReferenceGenome } from 'app/shared/model/enumerations/ensembl-reference-genome.model';

export interface IReferenceGenome {
  id?: number;
  version?: EnsemblReferenceGenome;
  ensemblGenes?: IEnsemblGene[] | null;
  alterations?: IAlteration[] | null;
}

export const defaultValue: Readonly<IReferenceGenome> = {};
