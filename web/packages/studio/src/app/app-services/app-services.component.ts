import { IAppState } from '../services/store';
import { AppActions } from '../services/app.actions';
import { Component, OnInit } from '@angular/core';
import { NgRedux, select } from '@angular-redux/store';
import { Router } from '@angular/router';

@Component({
  selector: 'app-services',
  templateUrl: './app-services.component.html',
  styleUrls: ['./app-services.component.scss']
})
export class AppServicesComponent implements OnInit {

  constructor(
    private actions: AppActions,
    private ngRedux: NgRedux<IAppState>,
    private router: Router) {
  }

  services = [1, 2, 3, 4, 5, 6, 7].map(i => {
    return {
      type: 'consumed',
      name: 'Service ' + i,
      actions: [
        { name: 'Action 1 ' + i },
        { name: 'Action 2 ' + i }
      ]
    }
  });

  ngOnInit() {
  }

  openComsumed() {
    this.router.navigate(['/consumed']);
  }

  openProvided() {
    this.router.navigate(['/provided']);
  }

}
