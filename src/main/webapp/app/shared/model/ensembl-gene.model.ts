import { ITranscript } from 'app/shared/model/transcript.model';
import { IReferenceGenome } from 'app/shared/model/reference-genome.model';
import { IGene } from 'app/shared/model/gene.model';

export interface IEnsemblGene {
  id?: number;
  ensemblGeneId?: string;
  canonical?: boolean;
  chromosome?: string;
  start?: number;
  end?: number;
  strand?: number;
  transcripts?: ITranscript[] | null;
  referenceGenome?: IReferenceGenome | null;
  gene?: IGene | null;
}

export const defaultValue: Readonly<IEnsemblGene> = {
  canonical: false,
};
