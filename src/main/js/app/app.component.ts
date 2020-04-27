import {Component} from '@angular/core';
import {QueueService} from "./queue.service";
import {interval} from "rxjs";

@Component({
  selector: 'app-root',
  template: `
      <h1>Add to queue</h1>
      <form #it="ngForm" (ngSubmit)="onSubmit(it)" novalidate>
          <textarea name="requests" ngModel required #request="ngModel" rows="10" cols="50"></textarea>
          <button>Submit</button>
      </form>
      <p>Queue:</p>
      <ul *ngFor="let server of servers()">
          {{ server }}
          <li *ngFor="let track of tracks(server)">
              {{ track }}
          </li>
      </ul>
      <router-outlet></router-outlet>
  `,
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'marvin';
  queue: any = {};
  constructor(private queueService: QueueService) {
    interval(5000).subscribe(() => {
      queueService.getQueue()
        .subscribe(data => {
          const ret = {};
          data.servers.forEach(server => ret[server.nick] = server.requests);
          this.queue = ret;
        })
    });
  }

  servers() {
    return Object.keys(this.queue)
  }

  tracks(nick) {
    return this.queue[nick]
  }

  onSubmit(it: any) {
    const requests = it.value.requests.split("\n")
      .filter(this.nonEmpty)
      .map(request => ({requests: [request], nick: request.match(/!([a-zA-Z0-9]+) .*/)[1]}));
    return this.queueService.enqueue({servers: requests})
      .subscribe(() => console.log('enqueued'));
  }

  private nonEmpty(str) {
    return str && str.length;
  }
}
