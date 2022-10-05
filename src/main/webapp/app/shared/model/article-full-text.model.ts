import { IArticle } from 'app/shared/model/article.model';

export interface IArticleFullText {
  id?: number;
  text?: string | null;
  html?: string | null;
  publicAccess?: boolean;
  article?: IArticle | null;
}

export const defaultValue: Readonly<IArticleFullText> = {
  publicAccess: false,
};
