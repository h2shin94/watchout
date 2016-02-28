void show_alert_window(void);
void hide_alert_window(void);

/**
* Defines alert list nodes as nodes of a doubly linked list
*/
typedef struct Alert_List_Node {
  struct Alert_List_Node *prev;
  struct Alert_List_Node *next;
  int alert_id;
  char *title;
  char *desc;
  int distance;
} Alert_List_Node;
Alert_List_Node *first_alert, *last_alert, *current_alert;

void add_alert(int, char *, char *, int);
Alert_List_Node *get_alert_with_id(int);
void remove_alert_node(Alert_List_Node *);
void refresh_alert_window(void);
void print_alert_list(void);
