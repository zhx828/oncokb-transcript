import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Col, Row, Table } from 'reactstrap';
import { byteSize, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntities } from './drug-synonym.reducer';
import { IDrugSynonym } from 'app/shared/model/drug-synonym.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IDrugSynonymProps extends StateProps, DispatchProps, RouteComponentProps<{ url: string }> {}

export const DrugSynonym = (props: IDrugSynonymProps) => {
  useEffect(() => {
    props.getEntities();
  }, []);

  const handleSyncList = () => {
    props.getEntities();
  };

  const { drugSynonymList, match, loading } = props;
  return (
    <div>
      <h2 id="drug-synonym-heading" data-cy="DrugSynonymHeading">
        Drug Synonyms
        <div className="d-flex justify-content-end">
          <Button className="mr-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh List
          </Button>
          <Link to={`${match.url}/new`} className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create new Drug Synonym
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {drugSynonymList && drugSynonymList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Drug</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {drugSynonymList.map((drugSynonym, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`${match.url}/${drugSynonym.id}`} color="link" size="sm">
                      {drugSynonym.id}
                    </Button>
                  </td>
                  <td>{drugSynonym.name}</td>
                  <td>{drugSynonym.drug ? <Link to={`drug/${drugSynonym.drug.id}`}>{drugSynonym.drug.id}</Link> : ''}</td>
                  <td className="text-right">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`${match.url}/${drugSynonym.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button tag={Link} to={`${match.url}/${drugSynonym.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button tag={Link} to={`${match.url}/${drugSynonym.id}/delete`} color="danger" size="sm" data-cy="entityDeleteButton">
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Delete</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Drug Synonyms found</div>
        )}
      </div>
    </div>
  );
};

const mapStateToProps = ({ drugSynonym }: IRootState) => ({
  drugSynonymList: drugSynonym.entities,
  loading: drugSynonym.loading,
});

const mapDispatchToProps = {
  getEntities,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(DrugSynonym);