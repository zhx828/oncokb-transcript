import React, { useEffect } from 'react';
import { connect } from 'app/shared/util/typed-inject';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootStore } from 'app/stores';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
export interface IReferenceGenomeDetailProps extends StoreProps, RouteComponentProps<{ id: string }> {}

export const ReferenceGenomeDetail = (props: IReferenceGenomeDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const referenceGenomeEntity = props.referenceGenomeEntity;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="referenceGenomeDetailsHeading">ReferenceGenome</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{referenceGenomeEntity.id}</dd>
          <dt>
            <span id="version">Version</span>
          </dt>
          <dd>{referenceGenomeEntity.version}</dd>
        </dl>
        <Button tag={Link} to="/reference-genome" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/reference-genome/${referenceGenomeEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStoreToProps = ({ referenceGenomeStore }: IRootStore) => ({
  referenceGenomeEntity: referenceGenomeStore.entity,
  getEntity: referenceGenomeStore.getEntity,
});

type StoreProps = ReturnType<typeof mapStoreToProps>;

export default connect(mapStoreToProps)(ReferenceGenomeDetail);
