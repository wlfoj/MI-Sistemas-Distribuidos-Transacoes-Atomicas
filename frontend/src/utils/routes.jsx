import React from 'react';
import { BrowserRouter, Switch, Route } from 'react-router-dom';
import Login from '../pages/Login';
import Inicial from '../pages/signup/Inicial';
import Physical from '../pages/signup/Physical';
import Juridical from '../pages/signup/Juridical';
import Joint from '../pages/signup/Joint';
import Home from '../pages/Home';
import Payment from '../pages/Payment';
import Deposit from '../pages/Deposit';
import Transfer from '../pages/Transfer';
import TransactionStatus from '../pages/TransactionStatus';



const Routes = () => (
	<BrowserRouter>
		<Switch>
			<Route exact path="/" component={Inicial} />
			<Route exact path="/login" component={Login} />
			<Route exact path="/home" component={Home} />
			<Route exact path="/physical" component={Physical} />
			<Route exact path="/juridical" component={Juridical} />
			<Route exact path="/joint" component={Joint} />
			<Route exact path="/payment" component={Payment} />
			<Route exact path="/deposit" component={Deposit} />
			<Route exact path="/transfer" component={Transfer} />
			<Route exact path="/transaction/:tid" component={TransactionStatus} />  Nova rota
		</Switch>
	</BrowserRouter>
);


export default Routes;