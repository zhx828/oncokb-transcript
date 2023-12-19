import React, { useEffect } from 'react';
import 'react-toastify/dist/ReactToastify.css';
import 'oncokb-styles/dist/oncokb.css';
import './app.scss';
import 'react-table/react-table.css';
import { componentInject } from 'app/shared/util/typed-inject';
import { observer } from 'mobx-react';
import { BrowserRouter as Router } from 'react-router-dom';
import { ToastContainer, toast } from 'react-toastify';
import { IRootStore } from 'app/stores';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import { AUTHORITIES } from 'app/config/constants/constants';
import AppRoutes from 'app/routes';
import NavigationSidebar from 'app/components/sidebar/NavigationSidebar';
import Layout from './layout';

const baseHref = document.querySelector('base').getAttribute('href').replace(/\/$/, '');

export type IAppProps = StoreProps;

const App: React.FunctionComponent<IAppProps> = (props: IAppProps) => {
  useEffect(() => {
    props.getSession();
  }, []);

  useEffect(() => {
    let authSubscriber = undefined;
    if (props.isCurator) {
      authSubscriber = props.initializeFirebase();
    }
    return () => authSubscriber && authSubscriber();
  }, [props.isCurator]);

  return (
    <Router basename={baseHref}>
      <Layout>
        <div className="app-container">
          <ToastContainer position={toast.POSITION.TOP_CENTER} className="toastify-container" toastClassName="toastify-toast" />
          <div>
            {props.isAuthorized && <NavigationSidebar />}
            <div className="app-center-content-wrapper" style={{ margin: props.centerContentMargin }}>
              <AppRoutes />
            </div>
          </div>
        </div>
      </Layout>
    </Router>
  );
};

const mapStoreToProps = ({ authStore, layoutStore, firebaseStore }: IRootStore) => ({
  isAuthorized: authStore.isAuthorized,
  isCurator: hasAnyAuthority(authStore.account.authorities, [AUTHORITIES.CURATOR]),
  getSession: authStore.getSession,
  navigationSidebarWidth: layoutStore.navigationSidebarWidth,
  toggleNavSidebar: layoutStore.toggleNavigationSidebar,
  centerContentMargin: layoutStore.centerContentMargin,
  initializeFirebase: firebaseStore.initializeFirebase,
});

type StoreProps = ReturnType<typeof mapStoreToProps>;

export default componentInject(mapStoreToProps)(observer(App));
