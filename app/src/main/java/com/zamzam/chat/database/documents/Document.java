package com.zamzam.chat.database.documents;

import java.util.List;

public interface Document<T> {

  public int size();
  public List<T> getList();

}
