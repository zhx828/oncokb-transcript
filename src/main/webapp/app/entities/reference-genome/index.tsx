import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import ReferenceGenome from './reference-genome';
import ReferenceGenomeDetail from './reference-genome-detail';
import ReferenceGenomeUpdate from './reference-genome-update';
import ReferenceGenomeDeleteDialog from './reference-genome-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={ReferenceGenomeUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={ReferenceGenomeUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={ReferenceGenomeDetail} />
      <ErrorBoundaryRoute path={match.url} component={ReferenceGenome} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={ReferenceGenomeDeleteDialog} />
  </>
);

export default Routes;
