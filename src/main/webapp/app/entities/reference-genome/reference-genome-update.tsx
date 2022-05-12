import React, { useState, useEffect } from 'react';
import { connect } from 'app/shared/util/typed-inject';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootStore } from 'app/stores';

import { IAlteration } from 'app/shared/model/alteration.model';
import { IReferenceGenome } from 'app/shared/model/reference-genome.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IReferenceGenomeUpdateProps extends StoreProps, RouteComponentProps<{ id: string }> {}

export const ReferenceGenomeUpdate = (props: IReferenceGenomeUpdateProps) => {
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const alterations = props.alterations;
  const referenceGenomeEntity = props.referenceGenomeEntity;
  const loading = props.loading;
  const updating = props.updating;
  const updateSuccess = props.updateSuccess;

  const handleClose = () => {
    props.history.push('/reference-genome');
  };

  useEffect(() => {
    if (isNew) {
      props.reset();
    } else {
      props.getEntity(props.match.params.id);
    }

    props.getAlterations({});
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...referenceGenomeEntity,
      ...values,
    };

    if (isNew) {
      props.createEntity(entity);
    } else {
      props.updateEntity(entity);
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          version: 'GRCh37',
          ...referenceGenomeEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="oncokbCurationApp.referenceGenome.home.createOrEditLabel" data-cy="ReferenceGenomeCreateUpdateHeading">
            Create or edit a ReferenceGenome
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField name="id" required readOnly id="reference-genome-id" label="ID" validate={{ required: true }} />
              ) : null}
              <ValidatedField label="Version" id="reference-genome-version" name="version" data-cy="version" type="select">
                <option value="GRCh37">GRCh37</option>
                <option value="GRCh38">GRCh38</option>
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/reference-genome" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

const mapStoreToProps = (storeState: IRootStore) => ({
  alterations: storeState.alterationStore.entities,
  referenceGenomeEntity: storeState.referenceGenomeStore.entity,
  loading: storeState.referenceGenomeStore.loading,
  updating: storeState.referenceGenomeStore.updating,
  updateSuccess: storeState.referenceGenomeStore.updateSuccess,
  getAlterations: storeState.alterationStore.getEntities,
  getEntity: storeState.referenceGenomeStore.getEntity,
  updateEntity: storeState.referenceGenomeStore.updateEntity,
  createEntity: storeState.referenceGenomeStore.createEntity,
  reset: storeState.referenceGenomeStore.reset,
});

type StoreProps = ReturnType<typeof mapStoreToProps>;

export default connect(mapStoreToProps)(ReferenceGenomeUpdate);
