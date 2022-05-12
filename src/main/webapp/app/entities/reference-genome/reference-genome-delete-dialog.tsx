import React, { useEffect } from 'react';
import { connect } from 'app/shared/util/typed-inject';
import { RouteComponentProps } from 'react-router-dom';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from 'reactstrap';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootStore } from 'app/stores';
export interface IReferenceGenomeDeleteDialogProps extends StoreProps, RouteComponentProps<{ id: string }> {}

export const ReferenceGenomeDeleteDialog = (props: IReferenceGenomeDeleteDialogProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const referenceGenomeEntity = props.referenceGenomeEntity;
  const updateSuccess = props.updateSuccess;

  const handleClose = () => {
    props.history.push('/reference-genome');
  };

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const confirmDelete = () => {
    props.deleteEntity(referenceGenomeEntity.id);
  };

  return (
    <Modal isOpen toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="referenceGenomeDeleteDialogHeading">
        Confirm delete operation
      </ModalHeader>
      <ModalBody id="oncokbCurationApp.referenceGenome.delete.question">Are you sure you want to delete this ReferenceGenome?</ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp; Cancel
        </Button>
        <Button id="jhi-confirm-delete-referenceGenome" data-cy="entityConfirmDeleteButton" color="danger" onClick={confirmDelete}>
          <FontAwesomeIcon icon="trash" />
          &nbsp; Delete
        </Button>
      </ModalFooter>
    </Modal>
  );
};

const mapStoreToProps = ({ referenceGenomeStore }: IRootStore) => ({
  referenceGenomeEntity: referenceGenomeStore.entity,
  updateSuccess: referenceGenomeStore.updateSuccess,
  getEntity: referenceGenomeStore.getEntity,
  deleteEntity: referenceGenomeStore.deleteEntity,
});

type StoreProps = ReturnType<typeof mapStoreToProps>;

export default connect(mapStoreToProps)(ReferenceGenomeDeleteDialog);
