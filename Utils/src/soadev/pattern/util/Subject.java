package soadev.pattern.util;

import java.util.List;

public interface Subject {
    List<Observer> getObservers();
    void setObservers(List<Observer> observers);
    void addObserver(Observer e);
    void removeObserver(Observer e);
    void notifyObservers();
}
