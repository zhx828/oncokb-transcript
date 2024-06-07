import React from 'react';
import DiffMatchPatch from 'diff-match-patch';
import Tabs from 'app/components/tabs/tabs';
import { RealtimeTextAreaInput } from 'app/shared/firebase/input/RealtimeInputs';
import { Container, Input } from 'reactstrap';
import classnames from 'classnames';
import * as styles from './style.module.scss';

type DiffViewerProps = {
  new: string;
  old: string;
  className?: string;
};
const DiffViewer: React.FunctionComponent<DiffViewerProps> = props => {
  const dmp = new DiffMatchPatch();
  const diff = dmp.diff_main(props.old, props.new);
  dmp.diff_cleanupSemantic(diff);
  return (
    <div className={props.className}>
      <Tabs
        className={'mx-0'}
        tabs={[
          {
            title: 'New',
            content: (
              <div>
                <Input type={'textarea'} value={props.new} style={{ backgroundColor: '#f1f9ff80' }} />
                <div className={classnames('mt-2', styles.diff)}>
                  <div className={'fw-bold'}>Difference comparing to the old</div>
                  <div dangerouslySetInnerHTML={{ __html: dmp.diff_prettyHtml(diff) }}></div>
                </div>
              </div>
            ),
          },
          {
            title: 'Old',
            content: <Input type={'textarea'} value={props.old} disabled />,
          },
        ]}
      />
    </div>
  );
};

export default DiffViewer;
